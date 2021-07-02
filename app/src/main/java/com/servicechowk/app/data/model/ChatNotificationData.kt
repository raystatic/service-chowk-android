package com.servicechowk.app.data.model

data class ChatNotificationData(
    val title:String,
    val body:String,
    val providerFCMToken:String,
    val providerId:String,
    val consumerId:String,
    val isConsumer:String
)