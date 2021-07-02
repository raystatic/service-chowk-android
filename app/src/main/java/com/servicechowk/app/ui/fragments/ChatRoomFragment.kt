package com.servicechowk.app.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.servicechowk.app.R
import com.servicechowk.app.data.model.User
import com.servicechowk.app.databinding.FragmentChatRoomBinding
import com.servicechowk.app.other.Extensions.showSnack
import com.servicechowk.app.other.Status
import com.servicechowk.app.ui.adapters.ChatRoomAdapter
import com.servicechowk.app.ui.viewmodels.ChatRoomViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatRoomFragment: Fragment(R.layout.fragment_chat_room) {

    private var _binding:FragmentChatRoomBinding?=null
    private val binding:FragmentChatRoomBinding get() = _binding!!

    private var providerId = ""

    private val vm by viewModels<ChatRoomViewModel>()

    private lateinit var chatRoomAdapter: ChatRoomAdapter

    private var providerFCMToken  = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatRoomBinding.bind(view)

        providerId = arguments?.getString("providerId") ?: ""
        providerFCMToken = arguments?.getString("providerFCMToken") ?: ""


        if (providerId.isEmpty() || providerFCMToken.isEmpty()) findNavController().navigateUp()

        chatRoomAdapter = ChatRoomAdapter {
            val bundle = bundleOf(
                "consumerId" to it.consumerId,
                "providerId" to it.providerId,
                "isConsumer" to false,
                "providerFCMToken" to providerFCMToken
            )
            findNavController().navigate(R.id.action_chatRoomFragment_to_chatFragment,bundle)
        }

        initUI()

        subscribeToObservers()

        vm.getChatsForRoom(providerId)

    }

    private fun subscribeToObservers() {
        vm.chatRooms.observe(viewLifecycleOwner,{
            binding.apply {
                when(it.status){
                    Status.SUCCESS -> {
                        val chatRooms = it.data
                        if (!chatRooms.isNullOrEmpty()){
                            rvchatRoom.isVisible = true
                            chatRoomAdapter.submitData(chatRooms)
                        }
                        progressbar.isVisible = false
                    }
                    Status.ERROR -> {
                        root.showSnack(it.message.toString())
                        progressbar.isVisible = false
                    }
                    Status.LOADING -> {
                        progressbar.isVisible  =true
                    }
                }
            }
        })
    }

    private fun initUI() {

        binding.apply {
            rvchatRoom.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = chatRoomAdapter
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}