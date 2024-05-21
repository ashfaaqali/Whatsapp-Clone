package com.ali.whatsappplus.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityVoiceCallBinding
import com.ali.whatsappplus.ui.fragment.PresenterFragment
import com.ali.whatsappplus.ui.fragment.call.IncomingCallFragment
import com.ali.whatsappplus.ui.fragment.call.OutgoingCallFragment
import com.ali.whatsappplus.util.Constants
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.model.GenerateToken
import com.cometchat.chat.core.CometChat

class VoiceCall : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    private var receiverId: String? = null
    private var receiverType: String? = null
    private var userName: String? = null
    private var userAvatar: String? = null
    private var callToken = ""
    private var dialog: AlertDialog? = null
    private var fragmentToLoad = ""
    private var TAG = "VoiceCallActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIntentData() // Get intent data

        if (fragmentToLoad == Constants.OUTGOING_CALL_FRAGMENT) { // Show the OutgoingCallFragment to the INITIATOR
            val args = Bundle()
            args.putString(Constants.USER_NAME, userName)
            args.putString(Constants.AVATAR, userAvatar)
            args.putString(Constants.RECEIVER_ID, receiverId)
            args.putString(Constants.RECEIVER_TYPE, receiverType)
            startFragment(OutgoingCallFragment(), args) // Show the fragment
        } else if (fragmentToLoad == Constants.PRESENTER_FRAGMENT) { // Show the PresenterFragment
            showProgressBar()
            Handler(Looper.getMainLooper()).postDelayed({
                val args = Bundle()
                args.putString(Constants.USER_NAME, userName)
                args.putString(Constants.AVATAR, userAvatar)
                args.putString(Constants.RECEIVER_ID, receiverId)
                args.putString(Constants.RECEIVER_TYPE, receiverType)
                args.putString(Constants.CALL_TOKEN, callToken)
                args.putBoolean(
                    Constants.IS_PRESENTER,
                    CometChat.getLoggedInUser().uid == "superhero1"
                )
                startFragment(PresenterFragment(), args) // Show the fragment
            }, 3000)
        } else if (fragmentToLoad == Constants.INCOMING_CALL_FRAGMENT) { // Show the IncomingCallFragment to the RECEIVER
            val args = Bundle()
            args.putString(Constants.USER_NAME, userName)
            args.putString(Constants.AVATAR, userAvatar)
            args.putString(Constants.RECEIVER_ID, receiverId)
            args.putString(Constants.RECEIVER_TYPE, receiverType)
            startFragment(IncomingCallFragment(), args) // Show the fragment
        }
    }

    override fun onResume() {
        super.onResume()
        val sessionId = "thanos" // Random or available in call object in case of default calling
        val userAuthToken = CometChat.getUserAuthToken() //Logged in user auth token

        CometChatCalls.generateToken(
            sessionId,
            userAuthToken,
            object : CometChatCalls.CallbackListener<GenerateToken>() {
                override fun onSuccess(generateToken: GenerateToken) {
                    callToken = generateToken.token
                    Log.d(TAG, "onSuccess: CallToken : ${generateToken.token}")
                }

                override fun onError(p0: com.cometchat.calls.exceptions.CometChatException?) {}
            }
        )
    }

    private fun showProgressBar() {
        // Inflate the custom view
        val dialogView = layoutInflater.inflate(R.layout.progress_bar, null)

        // Find the views in the custom layout
        // val progressBar: ProgressBar = dialogView.findViewById(R.id.progress_bar)

        // Create the AlertDialog
        dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog?.show()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog?.dismiss()
            dialog = null
        }, 3000) // 3000 milliseconds delay
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
            fragmentToLoad = intent.getStringExtra(Constants.FRAGMENT_TO_LOAD).toString()
        } else {
            Toast.makeText(applicationContext, "Error Loading Data", Toast.LENGTH_SHORT).show()
        }
    }
}