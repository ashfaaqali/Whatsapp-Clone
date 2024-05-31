package com.ali.whatsappplus

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.ali.whatsappplus.ui.activity.MainActivity
import com.ali.whatsappplus.util.Constants
import com.ali.whatsappplus.util.cometchatnotification.CometChatNotification
import com.cometchat.calls.core.CallAppSettings
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class Application : Application() {
    private val TAG = "Application"
    private lateinit var currentActivity: Activity

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(applicationContext)
        cometChatInit() // CometChat initialization
        cometChatCallsInit() // CometChatCalls initialization

    }

    // Initialize CometChat Calls
    private fun cometChatCallsInit() {
        val callAppSettings = CallAppSettings.CallAppSettingBuilder()
            .setAppId(Constants.APP_ID)
            .setRegion(Constants.REGION)
            .build()

        CometChatCalls.init(
            applicationContext,
            callAppSettings,
            object : CometChatCalls.CallbackListener<String>() {
                override fun onSuccess(s: String?) {
                    Log.d(TAG, "CometChatCalls initialization completed successfully")
                }

                override fun onError(p0: com.cometchat.calls.exceptions.CometChatException?) {
                    Log.d(
                        TAG,
                        "CometChatCalls initialization failed with exception: " + p0?.message
                    )
                }
            }
        )
    }

    // CometChat user login
    private fun loginUser(uid: String) {
        CometChat.login(
            uid,
            Constants.AUTH_KEY,
            object : CometChat.CallbackListener<User>() {
                override fun onSuccess(p0: User?) {
                    Log.d(TAG, "Login Successful : " + p0?.toString())
                    retrieveFcmRegistrationToken()
                }

                override fun onError(p0: CometChatException?) {
                    Log.d(TAG, "Login failed with exception: " + p0?.message)
                }

            }
        )
    }

    // Function to set current activity
    fun setCurrentActivity(activity: Activity) {
        currentActivity = activity
    }

    // Function to finish current activity
    fun finishCurrentActivity() {
        currentActivity.finish()
    }

    // Initialize CometChat (Must before calling any CometChat method)
    private fun cometChatInit() {
        val appSetting: AppSettings = AppSettings.AppSettingsBuilder()
            .setRegion(Constants.REGION)
            .subscribePresenceForAllUsers()
            .autoEstablishSocketConnection(true)
            .build()

        CometChat.init(
            this,
            Constants.APP_ID,
            appSetting,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) {
                    Log.d(TAG, "CometChat Initialization completed successfully")
                    loginUser("superhero1") // CometChat login
                }

                override fun onError(p0: CometChatException?) {
                    Log.d(TAG, "CometChat Initialization failed with exception: " + p0?.message)
                }

            }
        )
    }

    // Invoked when account is switched
    fun logoutAndLogin(uid: String) {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Log.d(TAG, "Logout Successful $p0")
                loginUser(uid)
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finishCurrentActivity()
                }, 3000)
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG, "Logout failed with exception: " + p0?.message)
            }
        })
    }

    private fun retrieveFcmRegistrationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            CometChatNotification.getInstance(this).registerCometChatPushNotificationToken(
                token,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(p0: String?) {
                        Log.e(TAG, "registerCometChatNotification onSuccess: $p0, Token : $token")

                    }

                    override fun onError(p0: CometChatException?) {
                        // TODO("Not yet implemented")
                    }

                })

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }
}