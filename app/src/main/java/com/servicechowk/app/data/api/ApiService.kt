package com.servicechowk.app.data.api

import com.servicechowk.app.data.model.ChatNotificationRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("send")
    suspend fun sendNotification(
        @Header("Authorization") secretKey:String,
        @Header("Content-Type") contentType:String,
        @Body chatNotificationRequest: ChatNotificationRequest
    )


}