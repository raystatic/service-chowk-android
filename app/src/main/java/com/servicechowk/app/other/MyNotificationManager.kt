package com.servicechowk.app.other

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.servicechowk.app.R
import com.servicechowk.app.ui.MainActivity

class MyNotificationManager(
    private var mCtx: Context,
    private var providerFCMToken:String,
    private var providerId:String,
    private var consumerId:String,
    private var isConsumer:String
) {
    fun displayNotification(title: String?, body: String?) {

        val mNotifyMgr =
            mCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mNotifyMgr.createNotificationChannel(mChannel)
        }

        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(mCtx, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)


        /*
        *  Clicking on the notification will take us to this intent
        *  Right now we are using the MainActivity as this is the only activity we have in our application
        *  But for your project you can customize it as you want
        * */
        val resultIntent = Intent(mCtx, MainActivity::class.java)
        resultIntent.putExtra("providerFCMToken",providerFCMToken)
        resultIntent.putExtra("providerId",providerId)
        resultIntent.putExtra("consumerId",consumerId)
        resultIntent.putExtra("isConsumer",isConsumer)

        /*
        *  Now we will create a pending intent
        *  The method getActivity is taking 4 parameters
        *  All paramters are describing themselves
        *  0 is the request code (the second parameter)
        *  We can detect this code in the activity that will open by this we can get
        *  Which notification opened the activity
        * */
        val pendingIntent =
            PendingIntent.getActivity(mCtx, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        /*
        *  Setting the pending intent to notification builder
        * */mBuilder.setContentIntent(pendingIntent)


        /*
        * The first parameter is the notification id
        * better don't give a literal here (right now we are giving a int literal)
        * because using this id we can modify it later
        * */mNotifyMgr.notify(1, mBuilder.build())
    }

    companion object {
        private var mInstance: MyNotificationManager? = null

        @Synchronized
        fun getInstance(
            context: Context,
            providerFCMToken:String,
            providerId:String,
            consumerId:String,
            isConsumer:String): MyNotificationManager? {
                if (mInstance == null) {
                    mInstance = MyNotificationManager(context,providerFCMToken, providerId, consumerId, isConsumer)
                }
                return mInstance
        }
    }


}