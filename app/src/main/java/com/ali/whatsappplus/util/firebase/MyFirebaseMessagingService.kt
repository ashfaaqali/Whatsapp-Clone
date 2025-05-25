package com.ali.whatsappplus.util.firebase

import android.util.Log
import com.ali.whatsappplus.util.cometchatnotification.CometChatNotification
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            if (CometChatNotification.isCometChatNotification(remoteMessage)) {
                CometChatNotification.renderCometChatNotification(
                    context = this,
                    remoteMessage = remoteMessage,
                    listener = object : CometChat.CallbackListener<String?>() {
                        override fun onSuccess(response: String?) {
                            Log.d(TAG, "CometChatNotification handled successfully: $response")
                        }

                        override fun onError(e: CometChatException?) {
                            Log.e(TAG, "Error handling CometChatNotification: ${e?.message}")
                        }
                    }
                )
            }

            // TODO: Add handling for non-CometChat notifications or long-running jobs if needed
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement actual server sync if needed
        Log.d(TAG, "sendRegistrationTokenToServer: $token")
    }
}