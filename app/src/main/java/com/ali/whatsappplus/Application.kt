package com.ali.whatsappplus

import android.app.Application
import android.util.Log
import com.cometchat.chat.helpers.Logger

class Application : Application() {

    companion object {
        private const val TAG = "Application"
        private const val DEFAULT_USER_ID = "ashfaq-ali"
    }

    override fun onCreate() {
        super.onCreate()

        if (!CometChat.isInitialized()) {
            CometChat.init(this) {

                Logger.enableLogs("221089")
                if (!CometChat.isCallsInitialized()) CometChat.initCometChatCalls(this)

                CometChat.login(uid = DEFAULT_USER_ID, onSuccess = {

                    CometChat.registerCallListeners(this)
                    CometChat.registerPushToken(this)

                }, onError = {
                    Log.e(TAG, "Login error: ${it?.message}")
                })
            }
        }
    }
}