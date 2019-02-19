package tech.vault.oauth.android

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

typealias VaultCallback<T> = (Result<T>) -> Unit

class VaultSDK private constructor(
        context: Context,
        val clientId: String,
        clientSecret: String,
        miningKey: String
) {

    data class Page<T>(val nextId: String?, val values: List<T>)

    class CallbackManager(
            private val callback: VaultCallback<String>
    ) {

        @Suppress("UNCHECKED_CAST")
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
                val serializableExtra = data.getSerializableExtra(AUTH_RESULT_KEY) as Result<String>
                callback(serializableExtra)
            }
        }
    }

    companion object {

        private var instance: VaultSDK? = null

        internal const val AUTH_RESULT_KEY = "AUTH_RESULT_KEY"

        internal const val vaultEndpoint = "https://2019-hackathon.api.mithvault.io"

        val sharedInstance: VaultSDK
            get() {
                return instance ?: error("try use sdk instance before configure")
            }

        val loggedIn: Boolean
            get() {
                return sharedInstance.pref.contains("authToken")
            }

        fun configure(context: Context, clientId: String, clientSecret: String, miningKey: String) {
            instance = VaultSDK(context, clientId, clientSecret, miningKey)
        }

        fun getAccessToken(activity: AppCompatActivity) {
            activity.startActivityForResult(
                    Intent(activity, VaultOAuthActivity::class.java),
                    300
            )
        }

        fun getUserInformation(callback: VaultCallback<VaultUserInfo>) {
            sharedInstance.vaultService.getUserInfo(callback)
        }

        fun getClientInformation(callback: VaultCallback<List<Balance>>) {
            sharedInstance.vaultService.getBalance(callback)
        }

        fun getUserMiningAction(nextId: String?, callback: VaultCallback<Page<MiningActivity>>) {
            sharedInstance.vaultService.getMiningActivities(nextId, callback)
        }

        fun postUserMiningAction(reward: Double, uuid: String, callback: VaultCallback<Void?>) {
            sharedInstance.vaultService.postUserMiningAction(reward, uuid, callback)
        }

        fun delUnbindToken(callback: VaultCallback<Void?>) {
            sharedInstance.vaultService.delUnbindToken(callback)
        }
    }

    private var retrofit = {
        val gson = GsonBuilder()
                .registerTypeAdapter(Date::class.java, GsonDateAdapter(context))
                .create()

        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        Retrofit.Builder()
                .client(client)
                .baseUrl(vaultEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }()

    internal val pref: SharedPreferences by lazy {
        context.getSharedPreferences("vault_oauth", Context.MODE_PRIVATE)
    }

    internal val vaultService = VaultService(
            retrofit.create(VaultRetrofitService::class.java),
            clientId,
            clientSecret,
            miningKey,
            pref
    )
}