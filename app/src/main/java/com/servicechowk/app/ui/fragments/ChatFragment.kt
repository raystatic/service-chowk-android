package com.servicechowk.app.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.servicechowk.app.R
import com.servicechowk.app.data.model.*
import com.servicechowk.app.databinding.FragmentChatBinding
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.PrefManager
import com.servicechowk.app.other.Status
import com.servicechowk.app.other.Utility
import com.servicechowk.app.ui.adapters.ChatAdapter
import com.servicechowk.app.ui.viewmodels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ChatFragment: Fragment(R.layout.fragment_chat) {

    private var _binding:FragmentChatBinding?=null
    private val binding:FragmentChatBinding get() = _binding!!

    private var isConsumer = false
    private var consumerId = ""
    private var providerId = ""

    private val vm by viewModels<ChatViewModel>()


    private lateinit var chatAdapter: ChatAdapter
    private var myId = ""
    private var provider:User?=null
    private var providerFCMToken = ""

    @Inject
    lateinit var auth: FirebaseAuth

    private var consumerFCMTOKENFROMPREVIOUSCHATS = ""

    @Inject
    lateinit var prefManager: PrefManager

    private var callPermissionGranted = false

    private lateinit var requrstPermissionLauncher:ActivityResultLauncher<String>

    private var phoneNumber = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        consumerId = arguments?.getString("consumerId") ?: ""
        providerId = arguments?.getString("providerId") ?: ""
        isConsumer = arguments?.getBoolean("isConsumer") ?: false
        providerFCMToken = arguments?.getString("providerFCMToken") ?: ""

        if (consumerId.isEmpty() || providerId.isEmpty() || providerFCMToken.isEmpty()) findNavController().navigateUp()

        myId = if (isConsumer) consumerId else providerId

        chatAdapter = ChatAdapter()

        requrstPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            if (isGranted){
                if (phoneNumber.isNotEmpty()){
                    makeCall(phoneNumber)
                }
            }else{
                Toast.makeText(requireContext(), "Permission not Granted", Toast.LENGTH_SHORT).show()
            }
        }

        initUI()

        subscribeToObservers()

        vm.getChats(consumerId, providerId)

        vm.getUserData(providerId)

    }

    private fun makeCall(phoneNumber: String) {
        val uri = "tel:${phoneNumber.trim()}"
        val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
        startActivity(intent)
    }

    private fun checkCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun subscribeToObservers() {

        vm.userData.observe(viewLifecycleOwner, {
            binding.apply {
                when(it.status){
                    Status.SUCCESS -> {
                        it.data?.let {
                            val phone = it.contactNumber?.substring(range = 3 until it.contactNumber?.length!!)
                            binding.imgCall.isVisible = true
                            binding.imgCall.setOnClickListener {
                                phoneNumber = phone.toString()
                                if (checkCallPermission()){
                                    makeCall(phoneNumber)
                                }else{
                                    requrstPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                }
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.imgCall.isVisible = false
                    }

                    Status.LOADING  -> {
                        binding.imgCall.isVisible = false
                    }
                }
            }
        })

        vm.addingChat.observe(viewLifecycleOwner,{
            binding.apply {
                when(it.status){
                    Status.SUCCESS -> {
                        imgSend.isEnabled = true
                        etChat.setText("")
                        rvChat.scrollToPosition(chatAdapter.itemCount-1)
                    }

                    Status.ERROR -> {
                        imgSend.isEnabled = true
                        root.showSnack(Constants.SOMETHING_WENT_WRONG)
                    }

                    Status.LOADING -> {
                        imgSend.isEnabled = false
                    }
                }
            }
        })

        vm.chats.observe(viewLifecycleOwner,{
            binding.apply {
                when(it.status){
                    Status.SUCCESS -> {
                        val chats = it.data
                        if (!chats.isNullOrEmpty()){
                            rvChat.isVisible =  true
                            chatAdapter.submitData(chats)
                            consumerFCMTOKENFROMPREVIOUSCHATS = chats[0].consumerFCMTOKEN ?: ""
                            rvChat.scrollToPosition(chatAdapter.itemCount-1)
                        }
                        progressbar.isVisible = false
                    }

                    Status.ERROR -> {
                        root.showSnack(Constants.SOMETHING_WENT_WRONG)
                        progressbar.isVisible = false
                    }

                    Status.LOADING -> {
                        progressbar.isVisible = true
                    }
                }
            }
        })
    }

    private fun initUI() {

        binding.apply {
            rvChat.apply {
                val mLayoutManager = LinearLayoutManager(requireContext())
                mLayoutManager.stackFromEnd = true
                layoutManager = mLayoutManager
                adapter = chatAdapter
            }

            imgBack.setOnClickListener {
                findNavController().navigateUp()
            }

            imgSend.setOnClickListener {
                val message = etChat.text.toString()
                if (message.isNotEmpty()){
                    val senderId = if (isConsumer) consumerId else providerId
                    val consumerFCMTOKEN = if (isConsumer) {
                        prefManager.getString(Constants.FCM_TOKEN)
                    }else{
                        if (consumerFCMTOKENFROMPREVIOUSCHATS.isEmpty())
                            prefManager.getString(Constants.FCM_TOKEN)
                        else
                            consumerFCMTOKENFROMPREVIOUSCHATS
                    }
                    val chat = Chat(
                        id = "${System.currentTimeMillis()}",
                        text = message,
                        createdAt = Utility.getCurrentTime(),
                        senderId = senderId,
                        sentByCustomer = isConsumer,
                        providerId = providerId,
                        consumerId = consumerId,
                        providerFCMTOKEN = providerFCMToken,
                        consumerFCMTOKEN = consumerFCMTOKEN
                    )
                    vm.addChat(chat, consumerId, providerId)

                    val toFCMTOKEN = if (isConsumer) chat.providerFCMTOKEN else chat.consumerFCMTOKEN

                    val setIsConsumer = if (isConsumer) "true" else "false"

                    val notification = ChatNotification(
                        title = "New Message Received",
                        body = chat.text.toString(),
                        providerFCMToken = chat.providerFCMTOKEN.toString(),
                        providerId = chat.providerId.toString(),
                        consumerId = chat.consumerId.toString(),
                        isConsumer = setIsConsumer,
                        sound = "default"
                    )

                    val data = ChatNotificationData(
                        title = "New Message Received",
                        body = chat.text.toString(),
                        providerFCMToken = chat.providerFCMTOKEN.toString(),
                        providerId = chat.providerId.toString(),
                        consumerId = chat.consumerId.toString(),
                        isConsumer = setIsConsumer,
                        sound = "default"
                    )

                    val chatNotificationRequest = ChatNotificationRequest(
                        to = toFCMTOKEN.toString(),
                        notification = notification,
                        data = data,
                        priority = 10,
                        android = ChatAndroidData("high"),
                        webpush = ChatWebPush(headers = ChatWebPushHeaders(Urgency = "high"))
                    )

                    vm.sendNotification(chatNotificationRequest)

                    println("TOFCMTOKEN: $toFCMTOKEN")

                }
            }

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}