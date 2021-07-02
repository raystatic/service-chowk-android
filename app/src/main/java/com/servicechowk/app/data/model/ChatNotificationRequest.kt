package com.servicechowk.app.data.model

data class ChatNotificationRequest(
    val to:String,
    val notification:ChatNotification,
    val data:ChatNotificationData
)