package com.ali.whatsappplus.util.cometchatnotification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.ali.whatsappplus.Application
import com.ali.whatsappplus.R
import com.ali.whatsappplus.ui.activity.MainActivity
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChatNotifications
import com.cometchat.chat.enums.PushPlatforms
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Objects


object CometChatNotification {
    private val TAG = "CometChatNotifications"
    // private lateinit var cometChatNotification: CometChatNotification
    private lateinit var notificationManager: NotificationManager

    fun getInstance(context: Context): CometChatNotification {
        return CometChatNotification
    }

    fun registerCometChatPushNotificationToken(pushToken: String, listener: CometChat.CallbackListener<String>){
//        if (!isFirebaseAppInitialized()) {
//            listener.onError(
//                CometChatException(
//                    "Notifications Not Registered",
//                    "FireBase App Not Initialized"
//                )
//            )
//            return
//        }

        CometChatNotifications.registerPushToken(
            pushToken,
            PushPlatforms.FCM_ANDROID,
            "cc_push_notification",
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(s: String) {
                    Log.e(TAG, "onSuccess:  CometChat Notification Registered : $s")
                    listener.onSuccess(s)
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "onError: Notification Registration Failed : " + e.message)
                    listener.onError(e)
                }
            })

    }

    fun isCometChatNotification(remoteMessage: RemoteMessage): Boolean {
        val data = JSONObject(remoteMessage.data as Map<String, String>)
        Log.d(TAG, "isCometChatNotification: RemoteMessage: " + remoteMessage.getData())
        Log.d(TAG, "isCometChatNotification: JSONObject: $data")
        return data.has(CometChatConstants.CATEGORY_MESSAGE)
    }

    fun renderCometChatNotification(
        remoteMessage: RemoteMessage,
        listener: CometChat.CallbackListener<String?>
    ) {
        val data = JSONObject(remoteMessage.data as Map<String, String>)
        Log.e(TAG, "renderCometChatNotification: Data $data")
        if (!data.has(CometChatConstants.CATEGORY_MESSAGE)) {
            listener.onError(
                CometChatException(
                    "Error",
                    "Not a CometChat Notification Data Payload"
                )
            )
            return
        }
        try {
            val rawMessageJson = JSONObject(data.getString(CometChatConstants.CATEGORY_MESSAGE))
            val baseMessage = getBaseMessage(rawMessageJson)
            Log.e(TAG, "renderCometChatNotification: BaseMessageType" + baseMessage!!.type)
            when (Objects.requireNonNull(baseMessage).type) {
                CometChatConstants.MESSAGE_TYPE_TEXT -> {
                    val textMessage = TextMessage.fromJson(rawMessageJson)
                    renderTextMessageNotification(textMessage)
                }

//                CometChatConstants.MESSAGE_TYPE_VIDEO -> {
//                    Log.e(TAG, "renderCometChatNotification: Video Call Received")
//                    val videoCallObject = Call.fromJson(rawMessageJson.toString())
//                    handleCallNotification(videoCallObject)
//                }
//
//                CometChatConstants.MESSAGE_TYPE_AUDIO -> {
//                    Log.e(TAG, "renderCometChatNotification: Audio Call Received")
//                    val audioCallObject = Call.fromJson(rawMessageJson.toString())
//                    handleCallNotification(audioCallObject)
//                }

                else -> {}
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Render Exception : " + e.message)
        }
    }

    private fun renderTextMessageNotification(message: TextMessage) {
        Log.e(TAG, "renderTextMessageNotification: $message")
        Log.e(TAG, "renderTextMessageNotification: Receiver type = " + message.receiverType)
        Log.e(TAG, "renderTextMessageNotification: Receiver type = " + message.receiver)
        if (message.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            showNotification(
                message.id,
                message.sender.name,
                message.text,
                message.sender.avatar,
                message.sender.toJson()
            )
        } else {
            showNotification(
                message.id,
                (message.receiver as Group).name,
                message.text,
                (message.receiver as Group).icon,
                groupToJson(message.receiver as Group)
            )
        }
    }

    private fun groupToJson(group: Group): JSONObject {
        val groupObject = JSONObject()
        try {
            groupObject.put(Group.COLUMN_GUID, group.guid)
            groupObject.put(Group.COLUMN_NAME, group.name)
            groupObject.put(Group.COLUMN_GROUP_TYPE, group.groupType)
            groupObject.put(Group.COLUMN_PASSWORD, group.password)
            groupObject.put(Group.COLUMN_ICON, group.icon)
            groupObject.put(Group.COLUMN_DESCRIPTION, group.description)
            groupObject.put(Group.COLUMN_OWNER, group.owner)
            groupObject.put(Group.COLUMN_METADATA, group.metadata)
            groupObject.put(Group.COLUMN_CREATED_AT, group.createdAt)
            groupObject.put(Group.COLUMN_UPDATED_AT, group.updatedAt)
            groupObject.put(Group.COLUMN_HAS_JOINED, group.joinedAt)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        return groupObject
    }

    private fun showNotification(
        nid: Int,
        title: String,
        text: String,
        largeIconUrl: String,
        payload: JSONObject
    ) {
        // Get the notification manager
        // Create the notification builder
        val context = Application.context
        val notificationManager: NotificationManager? = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val builder = NotificationCompat.Builder(context, "Messages")
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        if (!TextUtils.isEmpty(largeIconUrl)) {
            builder.setLargeIcon(getBitmapFromURL(largeIconUrl))
        } else {
            builder.setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_launcher_background
                )
            )
        }

        // Create the pending intent
        Log.e(TAG, "showNotification: Payload = $payload")
        val intent = Intent(
            context,
            MainActivity::class.java
        )
        intent.putExtra("notification_payload", payload.toString())
        val pendingIntent = PendingIntent.getActivity(
            Application.context,
            501,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)

        // show the notification
        notificationManager?.notify(nid, builder.build())
    }

    @Throws(JSONException::class)
    private fun getBaseMessage(messageObject: JSONObject): BaseMessage? {
        if (messageObject.has(CometChatConstants.MessageKeys.KEY_MESSAGE_CATEGORY)) {
            val category =
                messageObject.getString(CometChatConstants.MessageKeys.KEY_MESSAGE_CATEGORY)
            if (category.equals(CometChatConstants.CATEGORY_MESSAGE, ignoreCase = true)) {
                if (messageObject.has(CometChatConstants.MessageKeys.KEY_SEND_MESSAGE_TYPE)) {
                    val type =
                        messageObject.getString(CometChatConstants.MessageKeys.KEY_SEND_MESSAGE_TYPE)
                    return if (type.equals(
                            CometChatConstants.MESSAGE_TYPE_TEXT,
                            ignoreCase = true
                        )
                    ) {
                        TextMessage.fromJson(messageObject)
                    } else if (type.equals(
                            CometChatConstants.MESSAGE_TYPE_CUSTOM,
                            ignoreCase = true
                        )
                    ) {
                        CustomMessage.fromJson(messageObject)
                    } else {
                        MediaMessage.fromJson(messageObject)
                    }
                }
            } else if (category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true)) {
                return Action.fromJson(messageObject)
            } else if (category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)) {
                return Call.fromJson(messageObject.toString())
            } else if (category.equals(CometChatConstants.CATEGORY_CUSTOM, ignoreCase = true)) {
                return CustomMessage.fromJson(messageObject)
            }
        }
        return null
    }

    private fun getBitmapFromURL(strURL: String?): Bitmap? {
        return if (strURL != null) {
            try {
                val url = URL(strURL)
                val connection = url.openConnection() as HttpURLConnection
                connection.setDoInput(true)
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

//    private fun isFirebaseAppInitialized(): Boolean {
//        return FirebaseApp.getApps(context).isNotEmpty()
//    }
}