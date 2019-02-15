package tech.vault.oauth.android

import android.net.Uri
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


internal class VaultService(
        private val vaultRetrofitService: VaultRetrofitService,
        private val clientId: String,
        private val clientSecret: String
) {

    fun getAccessToken(resultUri: Uri, callback: (Result<String>) -> Unit) {
        val grantCode = resultUri.getQueryParameter("grant_code")
        val state = resultUri.getQueryParameter("state")
        if (grantCode != null && state != null) {
            val timeStamp = (System.currentTimeMillis() / 1000).toString()
            val nonce = Random().nextInt()
            val requestBody = RequestTokenBody(
                    clientId,
                    timeStamp,
                    nonce,
                    state,
                    grantCode
            )
            vaultRetrofitService
                    .getToken(
                            requestBody.toSig(clientSecret),
                            requestBody)
                    .enqueue(object : Callback<VaultRetrofitService.TokenBody> {
                        override fun onFailure(call: Call<VaultRetrofitService.TokenBody>, t: Throwable) {
                            callback(Result.failure(t))
                        }

                        override fun onResponse(call: Call<VaultRetrofitService.TokenBody>, response: Response<VaultRetrofitService.TokenBody>) {
                            response.body()?.let { callback(Result.success(it.token)) }
                        }
                    })
        }
    }

    fun getUserInfo(callback: VaultCallback<VaultUserInfo>) {
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = Random().nextInt()
        val sig = VaultPayload.build {
            dict(
                    "client_id" to value(clientId),
                    "timestamp" to value(timeStamp),
                    "nonce" to value(nonce)
            )
        }.toSignature(clientSecret)

        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)!!
        vaultRetrofitService
                .getUserInfo(
                        authToken,
                        sig,
                        clientId,
                        nonce,
                        timeStamp
                )
                .enqueue(object : Callback<VaultUserInfo> {
                    override fun onFailure(call: Call<VaultUserInfo>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<VaultUserInfo>, response: Response<VaultUserInfo>) {
                        response.body()?.let { callback(Result.success(it)) }
                    }
                })
    }

    fun getBalance(callback: VaultCallback<List<Balance>>) {
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = Random().nextInt()
        val sig = VaultPayload.build {
            dict(
                    "client_id" to value(clientId),
                    "timestamp" to value(timeStamp),
                    "nonce" to value(nonce)
            )
        }.toSignature(clientSecret)
        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)!!
        vaultRetrofitService
                .getBalance(
                        authToken,
                        sig,
                        clientId,
                        nonce,
                        timeStamp
                )
                .enqueue(object : Callback<List<Balance>> {
                    override fun onFailure(call: Call<List<Balance>>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<List<Balance>>, response: Response<List<Balance>>) {
                        response.body()?.let { callback(Result.success(it)) }
                    }
                })
    }
}