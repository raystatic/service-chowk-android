package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.servicechowk.app.R
import com.servicechowk.app.data.model.Chat
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.FragmentChatBinding
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Extensions.showSnack
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

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        consumerId = arguments?.getString("consumerId") ?: ""
        providerId = arguments?.getString("providerId") ?: ""
        isConsumer = arguments?.getBoolean("isConsumer") ?: false
        provider = arguments?.getParcelable("user")

        if (consumerId.isEmpty() || providerId.isEmpty() || provider == null) findNavController().navigateUp()

        myId = if (isConsumer) consumerId else providerId

        chatAdapter = ChatAdapter()

        initUI()

        subscribeToObservers()

        vm.getChats(consumerId, providerId)

    }

    private fun subscribeToObservers() {
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

            imgSend.setOnClickListener {
                val message = etChat.text.toString()
                if (message.isNotEmpty()){
                    val senderId = if (isConsumer) consumerId else providerId
                    val chat = Chat(
                        id = "${System.currentTimeMillis()}",
                        text = message,
                        createdAt = Utility.getCurrentTime(),
                        senderId = senderId,
                        sentByCustomer = isConsumer,
                        providerId = providerId,
                        consumerId = consumerId
                    )
                    vm.addChat(chat, consumerId, providerId)
                }
            }

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}