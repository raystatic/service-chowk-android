package com.servicechowk.app.data.model

import com.google.gson.annotations.SerializedName

data class ChatApnsHeaders(
        @SerializedName("apns-priority")
        val apnsPriority:String
)
