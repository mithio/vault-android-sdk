package tech.vault.oauth.android

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VaultSDK private constructor(
        val clientId: String,
        val clientSecret: String
) {

    sealed class AuthResult : Parcelable {
        @Parcelize
        data class Success(val token: String) : AuthResult()

        @Parcelize
        data class Failure(val error: Throwable) : AuthResult()
    }

    class CallbackManager(
            private val callback: (token: AuthResult) -> Unit
    ) {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
                callback(data.getParcelableExtra(AUTH_RESULT_KEY))
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

        fun configure(clientId: String, clientSecret: String) {
            instance = VaultSDK(clientId, clientSecret)
        }

        fun requestToken(activity: AppCompatActivity) {
            activity.startActivityForResult(
                    Intent(activity, VaultOAuthActivity::class.java),
                    300
            )
        }
    }

    private var retrofit = {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        Retrofit.Builder()
                .client(client)
                .baseUrl(vaultEndpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }()

    internal val vaultService = VaultService(
            retrofit.create(VaultRetrofitService::class.java),
            VaultSDK.sharedInstance.clientId,
            VaultSDK.sharedInstance.clientSecret
    )

}