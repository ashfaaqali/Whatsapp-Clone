package com.ali.whatsappplus.util.firebase

import android.util.Log
import com.ali.whatsappplus.util.cometchatnotification.CometChatNotification
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMessagingService"
    private lateinit var cometChatNotification: CometChatNotification

    override fun onCreate() {
        super.onCreate()
        cometChatNotification = CometChatNotification.getInstance(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            if (cometChatNotification.isCometChatNotification(remoteMessage)){
                cometChatNotification.renderCometChatNotification(remoteMessage, object : CometChat.CallbackListener<String?>(){
                    override fun onSuccess(p0: String?) {
                        // TODO("Not yet implemented")
                    }

                    override fun onError(p0: CometChatException?) {
                        // TODO("Not yet implemented")
                    }

                })
            }

            // Check if data needs to be processed by long running job
            // if (isLongRunningJob()) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                // scheduleJob()
            // } else {
                // Handle message within 10 seconds
                // handleNow()
            // }
        }
    }

//    private fun sendNotification(messageBody: String) {
//        val requestCode = 0
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            requestCode,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE,
//        )
//
//        val channelId = getString(R.string.default_notification_channel_id)
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(getString(R.string.fcm_message))
//            .setContentText(messageBody)
//            .setAutoCancel(true)
//            .setSound(defaultSoundUri)
//            .setContentIntent(pendingIntent)
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        val notificationId = 0
//        notificationManager.notify(notificationId, notificationBuilder.build())
//    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

}