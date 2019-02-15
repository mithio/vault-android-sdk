package tech.vault.oauth.android.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tech.vault.oauth.android.VaultSDK

class MainActivity : AppCompatActivity() {

    private val callbackManager: VaultSDK.CallbackManager = VaultSDK.CallbackManager { authResult ->
        authResult.onSuccess {

        }
        authResult.onFailure {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VaultSDK.configure(
                context = this,
                clientId = "ba6cabfb4de8d9f4f388124b1afe82b1",
                clientSecret = "aefd2b59d780eb29bc95b6cf8f3503233ad702141b20f53c8a645afbb8c6616048c5e9cc741e0ebee1a2469c68364e57e29dbeeabadc0b67958b9c3da7eabab9"
        )

        requestTokenButton.setOnClickListener {
            VaultSDK.requestToken(this)
        }

        if (VaultSDK.loggedIn) {
            userInfoButton.visibility = View.VISIBLE
        }

        userInfoButton.setOnClickListener {
            VaultSDK.getPersonalInfo { result ->
                result.onSuccess {

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}
