package com.servicechowk.app.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.servicechowk.app.R
import com.servicechowk.app.other.Utility
import kotlinx.android.synthetic.main.activity_main.*

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Utility.enableDarkTheme(false)

        val providerFCMToken = intent?.getStringExtra("providerFCMToken")
        val providerId = intent?.getStringExtra("providerId")
        val consumerId = intent?.getStringExtra("consumerId")
        val isConsumer = intent?.getStringExtra("isConsumer")

        if (!providerFCMToken.isNullOrEmpty() &&
            !providerId.isNullOrEmpty() &&
            !consumerId.isNullOrEmpty() &&
            !isConsumer.isNullOrEmpty()
        ){

            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra("providerFCMToken",providerFCMToken)
            intent.putExtra("providerId",providerId)
            intent.putExtra("consumerId",consumerId)
            intent.putExtra("isConsumer",isConsumer)

            startActivity(intent)
            finish()

        }

    }
}