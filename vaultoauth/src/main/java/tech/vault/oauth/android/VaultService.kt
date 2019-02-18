package tech.vault.oauth.android

import android.net.Uri
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

internal class VaultService(
        private val vaultRetrofitService: VaultRetrofitService,
        private val clientId: String,
        private val clientSecret: String,
        private val miningKey: String
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

    fun getMiningActivities(callback: VaultCallback<String>) {
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = Random().nextInt()
        val sig = VaultPayload.build {
            dict(
                    "client_id" to value(clientId),
                    "timestamp" to value(timeStamp),
                    "nonce" to value(nonce),
                    "mining_key" to value(miningKey)
            )
        }.toSignature(clientSecret)
        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)!!
        vaultRetrofitService
                .getMiningActivities(
                        authToken,
                        sig,
                        clientId,
                        nonce,
                        timeStamp,
                        miningKey
                )
                .enqueue(object : Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        response.body()?.let { callback(Result.success(it)) }
                    }
                })
    }

    fun mining(callback: VaultCallback<String>) {

        val happenedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Date())
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = Random().nextInt()
        val uuid = UUID.randomUUID().toString()
        val body = MiningBody(
                clientId,
                timeStamp,
                nonce,
                miningKey,
                uuid,
                10,
                happenedAt
        )

        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)!!
        val sig = body.toSig(clientSecret)
        vaultRetrofitService
                .mining(
                        sig,
                        authToken,
                        body
                )
                .enqueue(object : Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        response.body()?.let { callback(Result.success(it)) }
                    }
                })
    }

    fun unbind(callback: VaultCallback<Void>) {
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
                .unbind(
                        authToken,
                        sig,
                        clientId,
                        nonce,
                        timeStamp
                )
                .enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        response.body()?.let { callback(Result.success(it)) }
                    }
                })
    }
}