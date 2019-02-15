package tech.vault.oauth.android

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

data class VaultUserInfo(
        @SerializedName("kyc_level") val kycLevel: Int,
        @SerializedName("stake_level") val stakeLevel: Int,
        @SerializedName("balance") val balance: Double,
        @SerializedName("amount") val amount: Double,
        @SerializedName("staked_amount") val stakedAmount: Double
)


interface VaultRetrofitService {

    data class RequestTokenBody(
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

    @GET("oauth/user-info")
    fun getPersonalInfo(
            @Header("Authorization") authKey: String,
            @Header("X-Vault-Signature") sig: String,
            @Query("client_id") clientId: String,
            @Query("nonce") nonce: Int,
            @Query("timestamp") timestamp: String
    ): Call<VaultUserInfo>

}