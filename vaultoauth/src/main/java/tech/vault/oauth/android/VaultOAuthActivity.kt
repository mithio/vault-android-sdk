package tech.vault.oauth.android

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder

class VaultOAuthActivity : Activity() {

    sealed class AuthResult {
        data class Success(val token: String) : AuthResult()
        data class Failure(val error: Throwable) : AuthResult()
    }

    //TODO:
//    private val clientId by lazy {
//        getString(resources.getIdentifier("vault_client_id", null, null))
//    }
    private val clientId = "ba6cabfb4de8d9f4f388124b1afe82b1"

    private val redirectUri: Uri = Uri.parse("placeholder")
    private val authUri =
        Uri.parse("https://mining.mithvault.io/zh-TW/oauth/authorize?api=https://2019-hackathon.api.mithvault.io")
    private val tokenUri = Uri.parse("https://mithvault.io/oauth/token")

    private val authorizationServiceConfiguration = AuthorizationServiceConfiguration(authUri, tokenUri)

    private val appAuthConfiguration: AppAuthConfiguration =
        AppAuthConfiguration.Builder()
            .setBrowserMatcher(AnyBrowserMatcher.INSTANCE)
            .setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
            .build()

    private val authorizationService by lazy {
        AuthorizationService(this, appAuthConfiguration)
    }

    private val authRequest by lazy {
        AuthorizationRequest.Builder(
            authorizationServiceConfiguration,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        ).setAdditionalParameters(mapOf("device" to "android")).build()
    }
    private val customTabIntentBuilder by lazy {
        authorizationService.createCustomTabsIntentBuilder(authRequest.toUri())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acvitity_vault_oauth)

        if (intent.data == null) {
            val completionIntent = Intent(this, VaultOAuthActivity::class.java)
            val cancelIntent = Intent(this, VaultOAuthActivity::class.java)

            authorizationService.performAuthorizationRequest(
                authRequest,
                PendingIntent.getActivity(this, 0, completionIntent, 0),
                PendingIntent.getActivity(this, 0, cancelIntent, 0),
                customTabIntentBuilder.build()
            )
        }

//        vault-ba6cabfb4de8d9f4f388124b1afe82b1://
//        // grant_code=9857aab8fc779a34aeb55cca0284403ec4ae8b30e4017729793b523c03cfd6549abab548e9e91156b79e691ff94e4c953a0fa79073d34b474a46a3782170382e&state=sxpgwSyT--PqysSZrNtltw?code_challenge=90PgFlVAh_C680aU8zVwtE7OlvOU1IqCn3wxgsbXzLw
//        // &code_challenge_method=S256
//        // &device=android
//        // &redirect_uri=placeholder
//        // &response_type=code
    }

}