package tech.vault.oauth.android.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tech.vault.oauth.android.VaultSDK

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestTokenButton.setOnClickListener {
            VaultSDK.requestToken(this)
        }
    }
}
