package tech.vault.oauth.android

import retrofit2.Call
import retrofit2.http.*

interface VaultRetrofitService {

    class TokenBody(
            val token: String
    )

    @POST("oauth/token")
    fun getToken(
            @Header("X-Vault-Signature") sig: String,
            @Body body: RequestTokenBody
    ): Call<TokenBody>

    @GET("oauth/user-info")
    fun getUserInfo(
            @Header("Authorization") authKey: String,
            @Header("X-Vault-Signature") sig: String,
            @Query("client_id") clientId: String,
            @Query("nonce") nonce: Int,
            @Query("timestamp") timestamp: String
    ): Call<VaultUserInfo>

    @GET("oauth/balance")
    fun getBalance(
            @Header("Authorization") authKey: String,
            @Header("X-Vault-Signature") sig: String,
            @Query("client_id") clientId: String,
            @Query("nonce") nonce: Int,
            @Query("timestamp") timestamp: String
    ): Call<List<Balance>>


}