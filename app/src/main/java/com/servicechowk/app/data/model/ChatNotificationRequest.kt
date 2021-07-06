package com.servicechowk.app.data.model

data class ChatNotificationRequest(
    val to:String,
    val notification:ChatNotification,
    val data:ChatNotificationData,
    val priority:Int,
    val android:ChatAndroidData,
    val webpush:ChatWebPush
)