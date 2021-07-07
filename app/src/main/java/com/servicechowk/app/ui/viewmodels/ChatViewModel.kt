package com.servicechowk.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicechowk.app.data.model.Chat
import com.servicechowk.app.data.model.ChatNotificationRequest
import com.servicechowk.app.data.repositories.ChatRepository
import com.servicechowk.app.other.Constants
import com.servicechowk.app.other.Resource
import com.servicechowk.app.other.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
):ViewModel() {

    private val _chats = SingleLiveEvent<Resource<List<Chat>>>()
    val chats:SingleLiveEvent<Resource<List<Chat>>> get() = _chats

    private val _addingChat = SingleLiveEvent<Resource<Boolean>>()
    val addingChat:SingleLiveEvent<Resource<Boolean>> get() = _addingChat

    private val _userData = SingleLiveEvent<Resource<com.servicechowk.app.data.model.User>>()
    val userData:SingleLiveEvent<Resource<com.servicechowk.app.data.model.User>> get() = _userData

    fun sendNotification(chatNotificationRequest: ChatNotificationRequest) = viewModelScope.launch {
        repository.sendNotification(chatNotificationRequest)
    }

    fun getUserData(userId:String){
        _userData.postValue(Resource.loading(null))
        try{
            repository.getUser(userId)
                    .addSnapshotListener { value, error ->
                        if (error == null){
                            if (value == null || value.isEmpty){
                                _userData.postValue(Resource.success(null))
                            }else{
                                _userData.postValue(Resource.success(value.documents[0].toObject(com.servicechowk.app.data.model.User::class.java)))
                            }
                        }else{
                            _userData.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG,null))
                        }
                    }
        }catch (e: Exception){
            e.printStackTrace()
            _userData.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG, null))
        }
    }

    fun addChat(chat: Chat, consumerId: String, providerId: String){
        repository.addChat(chat,consumerId,providerId)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    _addingChat.postValue(Resource.success(true))
                }else{
                    _addingChat.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG,null))
                }
            }
    }

    fun getChats(consumerId:String, providerId:String){
        _chats.postValue(Resource.loading(null))
        repository.getChats(consumerId, providerId).also {
            it.addSnapshotListener { value, error ->
                if (error == null){
                    val chats = mutableListOf<Chat>()
                    if (value!=null && !value.isEmpty){
                        for (i in value.documents){
                            val chat = i.toObject(Chat::class.java)
                            chat?.let { it1 -> chats.add(it1) }
                        }
                    }
                    _chats.postValue(Resource.success(chats))
                }else{
                    _chats.postValue(Resource.error(Constants.SOMETHING_WENT_WRONG, null))
                }
            }
        }
    }

}