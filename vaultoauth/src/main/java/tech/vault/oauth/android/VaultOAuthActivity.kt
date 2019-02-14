package tech.vault.oauth.android

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class VaultOAuthActivity : Activity() {

    sealed class AuthResult {
        data class Success(val token: String) : AuthResult()
        data class Failure(val error: Throwable) : AuthResult()
    }

    //TODO:
//    private val clientId by lazy {
//        getString(resources.getIdentifier("vault_client_id", null, null))
//    }
    private val clientId = "ba6cabfb4de8d9f4f388124b1afe82b1"

    private val redirectUri: Uri = Uri.parse("placeholder")
    private val authUri =
            Uri.parse("https://mining.mithvault.io/zh-TW/oauth/authorize?api=https://2019-hackathon.api.mithvault.io")
    private val tokenUri = Uri.parse("https://2019-hackathon.api.mithvault.io")

    private val authorizationServiceConfiguration = AuthorizationServiceConfiguration(authUri, tokenUri)

    private val appAuthConfiguration: AppAuthConfiguration =
            AppAuthConfiguration.Builder()
                    .setBrowserMatcher(AnyBrowserMatcher.INSTANCE)
                    .setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
                    .build()

    private val authorizationService by lazy {
        AuthorizationService(this, appAuthConfiguration)
    }

    private val authRequest by lazy {
        AuthorizationRequest.Builder(
                authorizationServiceConfiguration,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
        ).setAdditionalParameters(mapOf("device" to "android")).build()
    }

    private val customTabIntentBuilder by lazy {
        authorizationService.createCustomTabsIntentBuilder(authRequest.toUri())
    }

    private var retrofit = {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        Retrofit.Builder()
                .client(client)
                .baseUrl("https://2019-hackathon.api.mithvault.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }()

    private val vaultService = retrofit.create(VaultService::class.java)

    private val pref: SharedPreferences by lazy {
        getSharedPreferences("vault_oauth", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acvitity_vault_oauth)

        val resultUri = intent.data
        if (resultUri == null) {
            val completionIntent = Intent(this, VaultOAuthActivity::class.java)
            val cancelIntent = Intent(this, VaultOAuthActivity::class.java)

            authorizationService.performAuthorizationRequest(
                    authRequest,
                    PendingIntent.getActivity(this, 0, completionIntent, 0),
                    PendingIntent.getActivity(this, 0, cancelIntent, 0),
                    customTabIntentBuilder.build()
            )
        } else {
            val grantCode = resultUri.toString().substringAfter("grant_code=").substringBefore("&")
            val state = resultUri.toString().substringAfter("state=").substringBefore("&")
            val timeStamp = (System.currentTimeMillis() / 1000).toString()
            val nonce = Random().nextInt()

            val requestBody = VaultService.RequestTokenBody(
                    clientId,
                    timeStamp,
                    nonce,
                    state,
                    grantCode
            )
            val call = vaultService.getToken(
                    requestBody.toSig("aefd2b59d780eb29bc95b6cf8f3503233ad702141b20f53c8a645afbb8c6616048c5e9cc741e0ebee1a2469c68364e57e29dbeeabadc0b67958b9c3da7eabab9"),
                    requestBody
            )
            call.enqueue(object : Callback<VaultService.TokenBody> {
                override fun onFailure(call: Call<VaultService.TokenBody>, t: Throwable) {
                }

                override fun onResponse(call: Call<VaultService.TokenBody>, response: Response<VaultService.TokenBody>) {
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        pref.edit()
                                .putString("authToken", responseBody.token)
                                .apply()
                    }
                }
            })
        }
    }
}