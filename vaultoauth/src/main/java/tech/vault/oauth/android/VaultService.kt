package tech.vault.oauth.android

import android.net.Uri
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.SecureRandom
import java.util.*

internal class VaultService(
        private val vaultRetrofitService: VaultRetrofitService,
        private val clientId: String,
        private val clientSecret: String
) {

    fun getAccessToken(resultUri: Uri, callback: (VaultSDK.AuthResult) -> Unit) {
        val grantCode = resultUri.getQueryParameter("grant_code")
        val state = resultUri.getQueryParameter("state")
        if (grantCode != null && state != null) {
            val timeStamp = (System.currentTimeMillis() / 1000).toString()
            val nonce = Random().nextInt()
            val requestBody = VaultRetrofitService.RequestTokenBody(
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
                            callback(VaultSDK.AuthResult.Failure(t))
                        }

                        override fun onResponse(call: Call<VaultRetrofitService.TokenBody>, response: Response<VaultRetrofitService.TokenBody>) {
                            response.body()?.let { callback(VaultSDK.AuthResult.Success(it.token)) }
                        }
                    })
        }
    }
}