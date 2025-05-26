package com.ali.whatsappplus

import android.content.Context
import android.content.Intent
import android.util.Log
import com.ali.whatsappplus.ui.call.VoiceCall
import com.ali.whatsappplus.common.util.Constants
import com.ali.whatsappplus.common.cometchatnotification.CometChatNotification
import com.cometchat.calls.core.CallAppSettings
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.google.firebase.messaging.FirebaseMessaging

object CometChat {
    private const val TAG = "CometChat"

    fun init(context: Context, onSuccess: () -> Unit) {
        val settings = AppSettings.AppSettingsBuilder()
            .subscribePresenceForAllUsers()
            .setRegion(AppCredentials.REGION)
            .autoEstablishSocketConnection(true)
            .build()

        CometChat.init(
            context,
            AppCredentials.APP_ID,
            settings,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(msg: String?) {
                    Log.i(TAG, "CometChat initialized.")
                    onSuccess()
                }

                override fun onError(e: CometChatException?) {
                    Log.e(TAG, "CometChat initialization failed: ${e?.message}")
                }
            })
    }

    fun initCometChatCalls(context: Context) {
        val callAppSettings = CallAppSettings.CallAppSettingBuilder()
            .setAppId(AppCredentials.APP_ID)
            .setRegion(AppCredentials.REGION)
            .build()

        CometChatCalls.init(
            context,
            callAppSettings,
            object : CometChatCalls.CallbackListener<String>() {
                override fun onSuccess(s: String?) {
                    Log.i(TAG, "CometChatCalls initialization completed successfully")
                }

                override fun onError(p0: com.cometchat.calls.exceptions.CometChatException?) {
                    Log.e(
                        TAG,
                        "CometChatCalls initialization failed with exception: " + p0?.message
                    )
                }
            }
        )
    }

    fun login(
        uid: String,
        onSuccess: (User) -> Unit,
        onError: (CometChatException?) -> Unit
    ) {
        if (CometChat.getLoggedInUser() != null) {
            Log.i(TAG, "Already logged in.")
            onSuccess(CometChat.getLoggedInUser()!!)
            return
        }

        CometChat.login(uid, AppCredentials.AUTH_KEY, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                Log.i(TAG, "Login success: ${user.name}")
                onSuccess(user)
            }

            override fun onError(e: CometChatException?) {
                Log.e(TAG, "Login error: ${e?.message}")
                onError(e)
            }
        })
    }

    fun logoutAndLogin(
        uid: String,
        onSuccess: () -> Unit = {},
        onError: (CometChatException?) -> Unit = {}
    ) {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(msg: String?) {
                login(uid, {}, {})
            }

            override fun onError(e: CometChatException?) {
                Log.e(TAG, "Logout error: ${e?.message}")
            }
        })
    }


    fun registerCallListeners(context: Context) {
        CometChat.addCallListener("call_listener", object : CometChat.CallListener() {
            override fun onIncomingCallReceived(call: Call?) {
                call?.let {
                    val intent = Intent(context, VoiceCall::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(Constants.CALL_SESSION_ID, call.sessionId)
                        putExtra(Constants.FRAGMENT_TO_LOAD, Constants.INCOMING_CALL_FRAGMENT)
                        putExtra(Constants.RECEIVER_TYPE, call.receiverType)

                        when (call.receiverType) {
                            CometChatConstants.RECEIVER_TYPE_USER -> {
                                putExtra(Constants.USER_NAME, call.sender.name)
                                putExtra(Constants.AVATAR, call.sender.avatar)
                                putExtra(Constants.RECEIVER_ID, call.sender.uid)
                            }
                            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                                val group = call.callReceiver as? Group
                                group?.let {
                                    putExtra(Constants.USER_NAME, it.name)
                                    putExtra(Constants.AVATAR, it.icon)
                                    putExtra(Constants.RECEIVER_ID, it.guid)
                                }
                            }
                        }
                    }
                    context.startActivity(intent)
                }
            }

            override fun onIncomingCallCancelled(call: Call?) {
                Log.d(TAG, "Call cancelled: $call")
            }

            override fun onOutgoingCallAccepted(call: Call?) {
                Log.d(TAG, "Call accepted: ${call?.callStatus}")
            }

            override fun onOutgoingCallRejected(call: Call?) {
                Log.d(TAG, "Call rejected: ${call?.callStatus}")
            }

            override fun onCallEndedMessageReceived(call: Call?) {
                Log.d(TAG, "Call ended: $call")
            }
        })
    }

    fun registerPushToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "FCM token fetch failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            CometChatNotification.registerCometChatPushNotificationToken(token, object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(msg: String?) {
                        Log.i(TAG, "FCM token registered: $token")
                    }

                    override fun onError(e: CometChatException) {
                        Log.e(TAG, "Push token registration error: ${e.message}")
                    }
                })
        }
    }

    fun isCallsInitialized(): Boolean = CometChatCalls.isInitialized()

    fun isInitialized(): Boolean = CometChat.isInitialized()
}

// CallbackListener class (abstract class for handling callbacks)
abstract class CallbackListener<T> {
    abstract fun onSuccess(t: T)
    abstract fun onError(e: String)
}
