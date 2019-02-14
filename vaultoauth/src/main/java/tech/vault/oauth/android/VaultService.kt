package tech.vault.oauth.android

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VaultService {

    class RequestTokenBody(
            @SerializedName("client_id")
            val clientId: String,
            val timestamp: String,
            val nonce: Int,
            val state: String,
            @SerializedName("grant_code")
            val grantCode: String
    ) {
        fun toSig(key: String): String {
            return VaultPayload.build {
                dict(
                        "client_id" to value(clientId),
                        "timestamp" to value(timestamp),
                        "nonce" to value(nonce),
                        "state" to value(state),
                        "grant_code" to value(grantCode)
                )
            }.toSignature(key)
        }
    }

    class TokenBody(
            val token: String
    )

    @POST("oauth/token")
    fun getToken(
            @Header("X-Vault-Signature") sig: String,
            @Body body: RequestTokenBody
    ): Call<TokenBody>
}