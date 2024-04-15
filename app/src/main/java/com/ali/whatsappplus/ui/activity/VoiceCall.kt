package com.ali.whatsappplus.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityVoiceCallBinding
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException

class VoiceCall : AppCompatActivity() {
    private lateinit var binding: ActivityVoiceCallBinding
    private var receiverId = ""
    private var receiverType = ""
    private var userName = ""
    private var userAvatar = ""

    private var TAG = "VoiceCallActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        statusBarTransparent()
        setContentView(binding.root)

        handleIntentData()
        setDataToViews()
        initiateVoiceCall()
    }

    private fun statusBarTransparent() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
        callListener()
    }

    private fun callListener() {
        CometChat.addCallListener(TAG, object : CometChat.CallListener() {
            override fun onOutgoingCallAccepted(p0: Call?) {
                Log.d(TAG, "Outgoing call accepted: " + p0?.toString())
            }

            override fun onIncomingCallReceived(p0: Call?) {
                Log.d(TAG, "Incoming call: " + p0?.toString())
            }

            override fun onIncomingCallCancelled(p0: Call?) {
                Log.d(TAG, "Incoming call cancelled: " + p0?.toString())
            }

            override fun onOutgoingCallRejected(p0: Call?) {
                Log.d(TAG, "Outgoing call rejected: " + p0?.toString())
            }

            override fun onCallEndedMessageReceived(p0: Call?) {
                Log.d(TAG, "End call message received: " + p0?.toString())
            }

        })
    }

    override fun onPause() {
        super.onPause()
        CometChat.removeCallListener(TAG)
    }

    private fun setDataToViews() {
        binding.contactName.text = userName
        Glide.with(this)
            .load(userAvatar)
            .into(binding.avatar)
    }

    private fun initiateVoiceCall() {

        val call = if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) Call(
            receiverId,
            CometChatConstants.RECEIVER_TYPE_USER,
            CometChatConstants.CALL_TYPE_AUDIO
        ) else Call(
            receiverId,
            CometChatConstants.RECEIVER_TYPE_GROUP,
            CometChatConstants.CALL_TYPE_AUDIO
        )

        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(call: Call) {
                Log.d(TAG, "Call initiated successfully: $call")
            }

            override fun onError(e: CometChatException) {
                Log.d(TAG, "Call initialization failed with exception: " + e.message)
            }
        })
    }

    private fun handleIntentData() {
        if (intent != null) {
            userName = intent.getStringExtra("name").toString()
            userAvatar = intent.getStringExtra("avatar").toString()
            receiverId = intent.getStringExtra("receiverId").toString()
            receiverType = intent.getStringExtra("receiverType").toString()
        } else {
            Toast.makeText(applicationContext, "Error Loading Data", Toast.LENGTH_SHORT).show()
        }
    }
}