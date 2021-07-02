package com.servicechowk.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicechowk.app.data.model.Chat
import com.servicechowk.app.data.model.ChatNotificationRequest
import com.servicechowk.app.data.model.ChatRoom
import com.servicechowk.app.data.repositories.ChatRepository
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Resource
import com.servicechowk.app.other.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val repository: ChatRepository
):ViewModel() {

    private val _chatRooms = SingleLiveEvent<Resource<List<ChatRoom>>>()
    val chatRooms:SingleLiveEvent<Resource<List<ChatRoom>>> get() = _chatRooms

    fun getChatsForRoom(providerId:String){
        _chatRooms.postValue(Resource.loading(null))
        repository.getChatsForRoom(providerId)
            .addSnapshotListener { value, error ->
                if (error == null){
                    val chatRooms = mutableListOf<ChatRoom>()
                    val chats = mutableListOf<Chat>()
                    if (value!=null && !value.isEmpty){
                        for (i in value.documents){
                            val chat = i.toObject(Chat::class.java)
                            if (chat != null) {
                                chats.add(chat)
                            }
                        }
                    }
                    val chatsByConsumerID = chats.groupBy {
                        it.consumerId
                    }

                    chatsByConsumerID.forEach {
                        chatRooms.add(
                            ChatRoom(
                                id = it.key.toString(),
                                time = it.value.last().createdAt.toString(),
                                providerId=it.value.last().providerId.toString(),
                                consumerId = it.value.last().consumerId.toString(),
                                text = it.value.last().text.toString()
                            )
                        )
                    }

                    _chatRooms.postValue(Resource.success(chatRooms))

                }else{
                    _chatRooms.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG,null))
                }
            }
    }

}