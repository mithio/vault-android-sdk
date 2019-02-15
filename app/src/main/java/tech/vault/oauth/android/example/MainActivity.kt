package tech.vault.oauth.android.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tech.vault.oauth.android.VaultSDK

class MainActivity : AppCompatActivity() {

    private val callbackManager: VaultSDK.CallbackManager = VaultSDK.CallbackManager { authResult ->
        when (authResult) {
            is VaultSDK.AuthResult.Success -> {

            }
            is VaultSDK.AuthResult.Failure -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VaultSDK.configure(
                clientId = "ba6cabfb4de8d9f4f388124b1afe82b1",
                clientSecret = "aefd2b59d780eb29bc95b6cf8f3503233ad702141b20f53c8a645afbb8c6616048c5e9cc741e0ebee1a2469c68364e57e29dbeeabadc0b67958b9c3da7eabab9"
        )

        requestTokenButton.setOnClickListener {
            VaultSDK.requestToken(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}
