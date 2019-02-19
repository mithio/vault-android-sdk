package tech.vault.oauth.android

import android.content.SharedPreferences
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
        private val miningKey: String,
        private val pref: SharedPreferences
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

        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)
                ?: error("try call vault api before get token")
        vaultRetrofitService
                .getUserInformation(
                        authToken,
                        sig,
                        clientId,
                        nonce,
                        timeStamp
                )
                .enqueue(object : Callback<VaultUserInfo> {
                    override fun onFailure(call: Call<VaultUserInfo>, t: Throwable) {
                        callback(Result.failure(t))
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
        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)
                ?: error("try call vault api before get token")
        vaultRetrofitService
                .getClientInformation(
                        authToken,
                        sig,
                        clientId,
                        nonce,
                        timeStamp
                )
                .enqueue(object : Callback<List<Balance>> {
                    override fun onFailure(call: Call<List<Balance>>, t: Throwable) {
                        callback(Result.failure(t))
                    }

                    override fun onResponse(call: Call<List<Balance>>, response: Response<List<Balance>>) {
                        response.body()?.let { callback(Result.success(it)) }
                    }
                })
    }

    fun getMiningActivities(nextId: String?, callback: VaultCallback<VaultSDK.Page<MiningActivity>>) {
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = Random().nextInt()
        val sig = VaultPayload.build {
            if (nextId != null) {
                dict(
                        "client_id" to value(clientId),
                        "timestamp" to value(timeStamp),
                        "nonce" to value(nonce),
                        "mining_key" to value(miningKey),
                        "next_id" to value(nextId)
                )
            } else {
                dict(
                        "client_id" to value(clientId),
                        "timestamp" to value(timeStamp),
                        "nonce" to value(nonce),
                        "mining_key" to value(miningKey)
                )
            }
        }.toSignature(clientSecret)
        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)
                ?: error("try call vault api before get token")
        val call = if (nextId != null) {
            vaultRetrofitService
                    .getUserMiningAction(
                            authToken,
                            sig,
                            clientId,
                            nonce,
                            timeStamp,
                            miningKey,
                            nextId
                    )

        } else {
            vaultRetrofitService
                    .getUserMiningAction(
                            authToken,
                            sig,
                            clientId,
                            nonce,
                            timeStamp,
                            miningKey
                    )

        }
        call.enqueue(object : Callback<List<MiningActivity>> {
            override fun onFailure(call: Call<List<MiningActivity>>, t: Throwable) {
                callback(Result.failure(t))
            }

            override fun onResponse(call: Call<List<MiningActivity>>, response: Response<List<MiningActivity>>) {
                response.body()?.let {
                    val resultPage = VaultSDK.Page(
                            response.headers()["X-Pagination-Next"],
                            it
                    )
                    callback(Result.success(resultPage))
                }

            }
        })
    }

    fun postUserMiningAction(reward: Double, uuid: String, callback: VaultCallback<Void?>) {

        val happenedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                .format(Date())
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = Random().nextInt()
        val body = MiningBody(
                clientId,
                timeStamp,
                nonce,
                miningKey,
                uuid,
                reward,
                happenedAt
        )

        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)
                ?: error("try call vault api before get token")
        val sig = body.toSig(clientSecret)
        vaultRetrofitService
                .postUserMiningAction(
                        sig,
                        authToken,
                        body
                )
                .enqueue(object : Callback<Void?> {
                    override fun onFailure(call: Call<Void?>, t: Throwable) {
                        callback(Result.failure(t))
                    }

                    override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                        response.body()?.let { callback(Result.success(null)) }
                    }
                })
    }

    fun delUnbindToken(callback: VaultCallback<Void?>) {
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = Random().nextInt()
        val sig = VaultPayload.build {
            dict(
                    "client_id" to value(clientId),
                    "timestamp" to value(timeStamp),
                    "nonce" to value(nonce)
            )
        }.toSignature(clientSecret)
        val authToken = VaultSDK.sharedInstance.pref.getString("authToken", null)
                ?: error("try call vault api before get token")
        vaultRetrofitService
                .delUnbindToken(
                        authToken,
                        sig,
                        clientId,
                        nonce,
                        timeStamp
                )
                .enqueue(object : Callback<Void?> {
                    override fun onFailure(call: Call<Void?>, t: Throwable) {
                        callback(Result.failure(t))
                    }

                    override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                        pref.edit()
                                .remove("authToken")
                                .apply()
                        callback(Result.success(null))
                    }
                })
    }
}