package com.servicechowk.app.data.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.servicechowk.app.data.model.Chat
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
){

    fun addChat(chat:Chat, consumerDeviceId:String, providerUserId:String):Task<Void>{
        return firestore.collection("chats")
            .document(providerUserId)
            .collection(consumerDeviceId)
            .document(chat.id.toString())
            .set(chat)
    }

    fun getChats(consumerId:String, providerId:String): Query {
        return firestore.collection("chats")
            .document(providerId)
            .collection(consumerId)
    }

}