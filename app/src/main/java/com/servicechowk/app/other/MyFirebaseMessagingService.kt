package com.servicechowk.app.other

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MyFirebaseMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var prefManager: PrefManager


    private val TAG =" NOTIFICATIONDEBUG"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG,"NotificationDebug received")

        var title = ""
        var body = ""
        var providerFCMToken = ""
        var providerId = ""
        var consumerId = ""
        var isConsumer = ""

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG,"NotificationDebug data payload: ${remoteMessage.data}")
            Log.d(TAG,"NotificationDebug Notification Body: ${remoteMessage.data["body"]}")
            title = "${remoteMessage.data["title"]}"
            body = "${remoteMessage.data["body"]}"
            providerFCMToken = "${remoteMessage.data["providerFCMToken"]}"
            providerId = "${remoteMessage.data["providerId"]}"
            consumerId = "${remoteMessage.data["consumerId"]}"
            isConsumer = "${remoteMessage.data["isConsumer"]}"
        }

        if (remoteMessage.notification!=null){
            Log.d(TAG,"NotificationDebug notification #${remoteMessage.notification!!.body}")
            MyNotificationManager.getInstance(
                context = this,
                providerFCMToken = providerFCMToken,
                providerId = providerId,
                consumerId = consumerId,
                isConsumer = isConsumer
            )!!.displayNotification(remoteMessage.data["title"], remoteMessage.data["body"])
        }
    }

    override fun onNewToken(token: String) {
        prefManager.saveString(Constants.FCM_TOKEN, token)
    }

}