package com.ali.whatsappplus

import android.app.Application
import android.util.Log
import com.ali.whatsappplus.util.Constants
import com.cometchat.calls.core.CallAppSettings
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User

class Application : Application() {
    private val TAG = "Application"

    override fun onCreate() {
        super.onCreate()
        //CometChat Initialization, Login
        cometChatInit()
        cometChatCallsInit()
        loginUser("superhero1")
    }

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
                    Log.d(TAG, "CometChatCalls initialization failed with exception: " + p0?.message)
                }
            }
        )
    }

    private fun loginUser(uid: String) {
        CometChat.login(
            uid,
            Constants.AUTH_KEY,
            object : CometChat.CallbackListener<User>() {
                override fun onSuccess(p0: User?) {
                    Log.d(TAG, "Login Successful : " + p0?.toString())
                }

                override fun onError(p0: CometChatException?) {
                    Log.d(TAG, "Login failed with exception: " + p0?.message)
                }

            }
        )
    }

    private fun cometChatInit() {
        val appSetting: AppSettings = AppSettings.AppSettingsBuilder()
            .setRegion(Constants.REGION)
            .subscribePresenceForAllUsers()
            .autoEstablishSocketConnection(true)
            .build();

        CometChat.init(
            this,
            Constants.APP_ID,
            appSetting,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) {
                    Log.d(TAG, "CometChat Initialization completed successfully")
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
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG, "Logout failed with exception: " + p0?.message)
            }
        })
    }
}