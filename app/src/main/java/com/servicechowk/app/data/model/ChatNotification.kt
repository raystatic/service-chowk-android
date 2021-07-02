package com.servicechowk.app.data.model

data class ChatNotification(
    val body:String,
    val title:String,
    val providerFCMToken:String,
    val providerId:String,
    val consumerId:String,
    val isConsumer:String
)