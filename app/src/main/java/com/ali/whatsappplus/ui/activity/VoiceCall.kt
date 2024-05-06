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
import androidx.fragment.app.Fragment
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityVoiceCallBinding
import com.ali.whatsappplus.ui.fragment.call.IncomingCallFragment
import com.ali.whatsappplus.ui.fragment.call.OutgoingCallFragment
import com.ali.whatsappplus.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException

class VoiceCall : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    private var receiverId: String? = null
    private var receiverType: String? = null
    private var userName: String? = null
    private var userAvatar: String? = null
    private var initiatedByUser: Boolean = false
    private var TAG = "VoiceCallActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIntentData() // Get intent data

        if (initiatedByUser){ // Show the OutgoingCallFragment to the INITIATOR
            val args = Bundle()
            args.putString(Constants.USER_NAME, userName)
            args.putString(Constants.AVATAR, userAvatar)
            args.putString(Constants.RECEIVER_ID, receiverId)
            args.putString(Constants.RECEIVER_TYPE, receiverType)
            startFragment(OutgoingCallFragment(), args) // Show the fragment
        } else { // Show the IncomingCallFragment to the RECEIVER
            val args = Bundle()
            args.putString(Constants.USER_NAME, userName)
            args.putString(Constants.AVATAR, userAvatar)
            args.putString(Constants.RECEIVER_ID, receiverId)
            args.putString(Constants.RECEIVER_TYPE, receiverType)
            startFragment(IncomingCallFragment(), args) // Show the fragment
        }
    }

    // Show fragment
    private fun startFragment(fragment: Fragment, args: Bundle? = null) {
        if (args != null) {
            fragment.arguments = args
            supportFragmentManager.beginTransaction().replace(R.id.frame_Layout, fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.frame_Layout, fragment)
                .commit()
        }
    }

    // Get intent data
    private fun handleIntentData() {
        if (intent != null) {
            userName = intent.getStringExtra(Constants.USER_NAME)
            userAvatar = intent.getStringExtra(Constants.AVATAR)
            receiverId = intent.getStringExtra(Constants.RECEIVER_ID)
            receiverType = intent.getStringExtra(Constants.RECEIVER_TYPE)
            initiatedByUser = intent.getBooleanExtra(Constants.INITIATED_BY_USER, false)
        } else {
            Toast.makeText(applicationContext, "Error Loading Data", Toast.LENGTH_SHORT).show()
        }
    }
}