package tech.vault.oauth.android.example

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tech.vault.oauth.android.VaultSDK
import java.util.*

class MainActivity : AppCompatActivity() {

    private val callbackManager: VaultSDK.CallbackManager = VaultSDK.CallbackManager { authResult ->
        authResult.onSuccess {
            updateView()
        }
        authResult.onFailure {
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VaultSDK.configure(
                context = this,
                clientId = "ba6cabfb4de8d9f4f388124b1afe82b1",
                clientSecret = "aefd2b59d780eb29bc95b6cf8f3503233ad702141b20f53c8a645afbb8c6616048c5e9cc741e0ebee1a2469c68364e57e29dbeeabadc0b67958b9c3da7eabab9",
                miningKey = "demo"
        )

        updateView()

        requestTokenButton.setOnClickListener {
            VaultSDK.getAccessToken(this)
        }

        userInfoButton.setOnClickListener {
            VaultSDK.getUserInformation { result ->
                result.onSuccess {
                    resultTextView.text = it.toString()
                }
                result.onFailure {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        clientInfoButton.setOnClickListener {
            VaultSDK.getClientInformation { result ->
                result.onSuccess {
                    resultTextView.text = it.toString()
                }
                result.onFailure {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        mine10RewardButton.setOnClickListener {
            VaultSDK.postUserMiningAction(10.0, UUID.randomUUID().toString()) {

            }
        }
        showMiningActivitiesButton.setOnClickListener {
            VaultSDK.getUserMiningAction(null) { result ->
                result.onSuccess {
                    resultTextView.text = it.toString()
                }
                result.onFailure {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        unbindButton.setOnClickListener {
            VaultSDK.delUnbindToken {
                updateView()
            }
        }
    }

    private fun updateView() {
        if (VaultSDK.loggedIn) {
            requestTokenButton.visibility = GONE
            showMiningActivitiesButton.visibility = VISIBLE
            userInfoButton.visibility = VISIBLE
            unbindButton.visibility = VISIBLE
            mine10RewardButton.visibility = VISIBLE
            unbindButton.visibility = VISIBLE
            clientInfoButton.visibility = VISIBLE
        } else {
            requestTokenButton.visibility = VISIBLE
            showMiningActivitiesButton.visibility = GONE
            userInfoButton.visibility = GONE
            unbindButton.visibility = GONE
            mine10RewardButton.visibility = GONE
            unbindButton.visibility = GONE
            clientInfoButton.visibility = GONE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}
