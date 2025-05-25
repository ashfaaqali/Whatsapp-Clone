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
import com.ali.whatsappplus.R
import com.ali.whatsappplus.ui.main.MainActivity
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChatNotifications
import com.cometchat.chat.enums.PushPlatforms
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.*
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object CometChatNotification {

    private const val TAG = "CometChatNotifications"
    private const val CHANNEL_ID = "Messages"
    private const val NOTIFICATION_ID = 501

    // Public entry point: Token registration
    fun registerCometChatPushNotificationToken(
        pushToken: String,
        listener: CometChat.CallbackListener<String>
    ) {
        CometChatNotifications.registerPushToken(
            pushToken,
            PushPlatforms.FCM_ANDROID,
            "cc_push_notification",
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(s: String) {
                    Log.i(TAG, "Notification Token Registered: $s")
                    listener.onSuccess(s)
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "Notification Token Registration Failed: ${e.message}")
                    listener.onError(e)
                }
            })
    }

    // Check if it's a CometChat push
    fun isCometChatNotification(remoteMessage: RemoteMessage): Boolean {
        return remoteMessage.data.containsKey(CometChatConstants.CATEGORY_MESSAGE)
    }

    // Parse and render notification
    fun renderCometChatNotification(
        context: Context,
        remoteMessage: RemoteMessage,
        listener: CometChat.CallbackListener<String?>
    ) {
        try {
            val data = JSONObject(remoteMessage.data as Map<String, String>)
            if (!data.has(CometChatConstants.CATEGORY_MESSAGE)) {
                listener.onError(
                    CometChatException("Error", "Not a CometChat notification payload")
                )
                return
            }

            val rawMessage = JSONObject(data.getString(CometChatConstants.CATEGORY_MESSAGE))
            val baseMessage = parseBaseMessage(rawMessage)

            baseMessage?.let {
                when (it.type) {
                    CometChatConstants.MESSAGE_TYPE_TEXT -> {
                        val message = it as TextMessage
                        renderTextMessageNotification(context, message)
                        listener.onSuccess("Rendered Text Message")
                    }
                    // Uncomment to handle call types
//                    CometChatConstants.MESSAGE_TYPE_AUDIO,
//                    CometChatConstants.MESSAGE_TYPE_VIDEO -> {
//                        val call = it as Call
//                        handleCallNotification(context, call)
//                        listener.onSuccess("Rendered Call")
//                    }

                    else -> {
                        Log.w(TAG, "Unsupported message type: ${it.type}")
                        listener.onSuccess("Unsupported Message Type")
                    }
                }
            } ?: run {
                listener.onError(CometChatException("Parse Error", "Invalid message"))
            }

        } catch (e: JSONException) {
            Log.e(TAG, "Notification Render Error: ${e.message}")
            listener.onError(CometChatException("JSON Error", e.message ?: ""))
        }
    }

    // Renders Text Message Notification
    private fun renderTextMessageNotification(context: Context, message: TextMessage) {
        val title: String
        val avatar: String
        val payload: JSONObject

        if (message.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            title = message.sender.name
            avatar = message.sender.avatar
            payload = message.sender.toJson()
        } else {
            val group = message.receiver as? Group ?: return
            title = group.name
            avatar = group.icon
            payload = groupToJson(group)
        }

        showNotification(context, message.id, title, message.text, avatar, payload)
    }

    // Builds and shows the Android Notification
    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        avatarUrl: String?,
        payload: JSONObject
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("notification_payload", payload.toString())
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(message)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            setLargeIcon(getBitmapFromURL(context, avatarUrl))
        }

        notificationManager.notify(notificationId, builder.build())
    }

    // Parses base message from JSON
    @Throws(JSONException::class)
    private fun parseBaseMessage(json: JSONObject): BaseMessage? {
        return when (json.optString(CometChatConstants.MessageKeys.KEY_MESSAGE_CATEGORY)) {
            CometChatConstants.CATEGORY_MESSAGE -> {
                when (json.optString(CometChatConstants.MessageKeys.KEY_SEND_MESSAGE_TYPE)) {
                    CometChatConstants.MESSAGE_TYPE_TEXT -> TextMessage.fromJson(json)
                    CometChatConstants.MESSAGE_TYPE_CUSTOM -> CustomMessage.fromJson(json)
                    else -> MediaMessage.fromJson(json)
                }
            }

            CometChatConstants.CATEGORY_ACTION -> Action.fromJson(json)
            CometChatConstants.CATEGORY_CALL -> Call.fromJson(json.toString())
            CometChatConstants.CATEGORY_CUSTOM -> CustomMessage.fromJson(json)
            else -> null
        }
    }

    // Converts group to JSON payload
    private fun groupToJson(group: Group): JSONObject = JSONObject().apply {
        put(Group.COLUMN_GUID, group.guid)
        put(Group.COLUMN_NAME, group.name)
        put(Group.COLUMN_GROUP_TYPE, group.groupType)
        put(Group.COLUMN_PASSWORD, group.password)
        put(Group.COLUMN_ICON, group.icon)
        put(Group.COLUMN_DESCRIPTION, group.description)
        put(Group.COLUMN_OWNER, group.owner)
        put(Group.COLUMN_METADATA, group.metadata)
        put(Group.COLUMN_CREATED_AT, group.createdAt)
        put(Group.COLUMN_UPDATED_AT, group.updatedAt)
        put(Group.COLUMN_HAS_JOINED, group.joinedAt)
    }

    // Loads image for notification
    private fun getBitmapFromURL(context: Context, strURL: String?): Bitmap {
        return try {
            if (strURL.isNullOrBlank()) {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background)
            } else {
                val url = URL(strURL)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                BitmapFactory.decodeStream(connection.inputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background)
        }
    }
}