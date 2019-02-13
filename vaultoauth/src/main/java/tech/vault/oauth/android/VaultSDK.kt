package tech.vault.oauth.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent

@SuppressLint("StaticFieldLeak")
class VaultSDK private constructor(
    private val appContext: Context
) {

    companion object {

        private var instance: VaultSDK? = null

        val sharedInstance: VaultSDK
            get() {
                return instance ?: error("try use sdk instance before configure")
            }

        fun configure(context: Context) {
            instance = VaultSDK(context)
        }

        fun requestToken(activity: Activity) {
            activity.startActivity(Intent(activity, VaultOAuthActivity::class.java))
        }
    }

}