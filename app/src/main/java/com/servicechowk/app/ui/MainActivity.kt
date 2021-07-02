package com.servicechowk.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.servicechowk.app.R
import com.servicechowk.app.databinding.ActivityMainBinding
import com.servicechowk.app.other.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            val boolIsConsumer = isConsumer == "false"
            val bundle = bundleOf(
                "consumerId" to consumerId,
                "providerId" to providerId,
                "isConsumer" to boolIsConsumer,
                "providerFCMToken" to providerFCMToken
            )
            nav_host_fragment.findNavController().navigate(R.id.action_to_chat_fragment, bundle)
        }

    }
}