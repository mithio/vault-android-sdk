package tech.vault.oauth.android

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


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
    private val tokenUri = Uri.parse("https://mithvault.io/oauth/token")

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acvitity_vault_oauth)

        val resultUri = intent.data
        if (resultUri != null) {
            val grantCodeSubString = resultUri.toString().substringAfter("grant_code=")
            val grantCode = grantCodeSubString.substringBefore("&")
            Log.d("VaultOauth", "code: $grantCode")

            GetUrlContentTask(grantCode)
                    .execute("https://mining.mithvault.io/zh-TW/oauth/token?api=https://2019-hackathon.api.mithvault.io")
        } else {
            val completionIntent = Intent(this, VaultOAuthActivity::class.java)
            val cancelIntent = Intent(this, VaultOAuthActivity::class.java)

            authorizationService.performAuthorizationRequest(
                    authRequest,
                    PendingIntent.getActivity(this, 0, completionIntent, 0),
                    PendingIntent.getActivity(this, 0, cancelIntent, 0),
                    customTabIntentBuilder.build()
            )
        }

//        vault-ba6cabfb4de8d9f4f388124b1afe82b1://
//        // grant_code=9857aab8fc779a34aeb55cca0284403ec4ae8b30e4017729793b523c03cfd6549abab548e9e91156b79e691ff94e4c953a0fa79073d34b474a46a3782170382e&state=sxpgwSyT--PqysSZrNtltw?code_challenge=90PgFlVAh_C680aU8zVwtE7OlvOU1IqCn3wxgsbXzLw
//        // &code_challenge_method=S256
//        // &device=android
//        // &redirect_uri=placeholder
//        // &response_type=code
    }
}

private class GetUrlContentTask(val grantCode: String) : AsyncTask<String, Int, String>() {
    override fun doInBackground(vararg urls: String): String {
        val url = URL(urls[0])
        val connection = url.openConnection() as HttpURLConnection

        val timeStamp = (System.currentTimeMillis() / 1000).toString()

        val sig = VaultPayload.build {
            dict("client_id" to value("ba6cabfb4de8d9f4f388124b1afe82b1"))
            dict("timestamp" to value(timeStamp))
            dict("nonce" to value("234650723406"))
            dict("state" to value("asfajskd2452"))
            dict("grant_code" to value(grantCode))
        }.toSignature("aefd2b59d780eb29bc95b6cf8f3503233ad702141b20f53c8a645afbb8c6616048c5e9cc741e0ebee1a2469c68364e57e29dbeeabadc0b67958b9c3da7eabab9")

        connection.requestMethod = "POST"
        connection.setRequestProperty("X-Vault-Signature", sig)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 5000
        connection.readTimeout = 5000


        val jsonString = JSONObject()
                .put("client_id", "ba6cabfb4de8d9f4f388124b1afe82b1")
                .put("timestamp", timeStamp)
                .put("nonce", "234650723406")
                .put("state", "asfajskd2452")
                .put("grant_code", grantCode)
                .toString()

        val os = connection.outputStream
        os.write(jsonString.toByteArray())
        os.close()

        connection.connect()
        val rd = BufferedReader(InputStreamReader(connection.inputStream))
        var content = ""
        var line = rd.readLine()
        while (line != null) {
            content += line + "\n"
            line = rd.readLine()
        }
        return content
    }

    override fun onProgressUpdate(vararg values: Int?) {
    }

    override fun onPostExecute(result: String) {

    }
}
