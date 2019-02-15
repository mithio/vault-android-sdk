package tech.vault.oauth.android

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder


class VaultOAuthActivity : Activity() {

    private val authUri =
            Uri.parse("https://mining.mithvault.io/zh-TW/oauth/authorize?api=${VaultSDK.vaultEndpoint}")

    private val authorizationServiceConfiguration = AuthorizationServiceConfiguration(authUri, Uri.parse(VaultSDK.vaultEndpoint))

    private val appAuthConfiguration: AppAuthConfiguration =
            AppAuthConfiguration.Builder()
                    .setBrowserMatcher(AnyBrowserMatcher.INSTANCE)
                    .setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
                    .build()

    private val authorizationService by lazy {
        AuthorizationService(this, appAuthConfiguration)
    }

    private val authRequest by lazy {
        AuthorizationRequest
                .Builder(
                        authorizationServiceConfiguration,
                        VaultSDK.sharedInstance.clientId,
                        ResponseTypeValues.CODE,
                        Uri.parse("placeholderServerWontUse")
                )
                .setAdditionalParameters(mapOf("device" to "android")).build()
    }

    private val customTabIntentBuilder by lazy {
        authorizationService.createCustomTabsIntentBuilder(authRequest.toUri())
    }

    private val pref: SharedPreferences by lazy {
        getSharedPreferences("vault_oauth", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acvitity_vault_oauth)
        val completionIntent = Intent(this, VaultOAuthActivity::class.java)
        val cancelIntent = Intent(this, VaultOAuthActivity::class.java)
        authorizationService.performAuthorizationRequest(
                authRequest,
                PendingIntent.getActivity(this, 0, completionIntent, 0),
                PendingIntent.getActivity(this, 0, cancelIntent, 0),
                customTabIntentBuilder.build()
        )
    }

    override fun onNewIntent(intent: Intent?) {
        val resultUri = intent?.data
        if (resultUri != null) {
            VaultSDK.sharedInstance.vaultService.getAccessToken(resultUri) { result ->
                when (result) {
                    is VaultSDK.AuthResult.Success -> {
                        pref.edit()
                                .putString("authToken", result.token)
                                .apply()
                    }
                    is VaultSDK.AuthResult.Failure -> {

                    }
                }
                setResult(RESULT_OK, Intent().putExtra(VaultSDK.AUTH_RESULT_KEY, result))
            }
        }
    }
}