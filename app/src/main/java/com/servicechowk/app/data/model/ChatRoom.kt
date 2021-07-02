package com.servicechowk.app.data.model

data class ChatRoom(
    val id:String,
    val time:String,
    val providerId:String,
    val consumerId:String,
    val text:String
)