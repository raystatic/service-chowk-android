package com.servicechowk.app.data.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.servicechowk.app.data.api.ApiService
import com.servicechowk.app.data.model.Chat
import com.servicechowk.app.data.model.ChatNotificationRequest
import com.servicechowk.app.other.Constants
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val apiService: ApiService
){

    fun addChat(chat:Chat, consumerDeviceId:String, providerUserId:String):Task<Void>{
        return firestore.collection("chats")
            .document(chat.id.toString())
            .set(chat)
    }

    fun getChats(consumerId:String, providerId:String): Query {
        return firestore.collection("chats")
            .whereEqualTo("consumerId",consumerId)
            .whereEqualTo("providerId",providerId)
    }

    fun getChatsForRoom(providerId: String): Query {
        return firestore.collection("chats")
            .whereEqualTo("providerId",providerId)
    }

    fun getUser(userId:String):Query{
        return firestore.collection("users").whereEqualTo("id",userId)
    }

    suspend fun sendNotification(chatNotificationRequest: ChatNotificationRequest){
        apiService.sendNotification(Constants.FIREBASE_SECRET_KEY,"application/json",chatNotificationRequest)
    }

}