package com.servicechowk.app.data.model

data class Chat(
    val id:String?="",
    val text:String?="",
    val createdAt:String?="",
    val senderId:String?="",
    val sentByCustomer:Boolean?=false,
    val providerId:String?="",
    val consumerId:String?=""
)
