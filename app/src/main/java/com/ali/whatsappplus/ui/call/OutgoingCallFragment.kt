package com.ali.whatsappplus.ui.call

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ali.whatsappplus.databinding.FragmentOutgoingCallBinding
import com.ali.whatsappplus.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException

class OutgoingCallFragment : Fragment() {

    private lateinit var binding: FragmentOutgoingCallBinding
    private var receiverId: String? = null
    private var receiverType: String? = null
    private var userName: String? = null
    private var userAvatar: String? = null
    private var sessionID: String? = null
    private val TAG = "OutgoingCallFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOutgoingCallBinding.inflate(layoutInflater)
        statusBarTransparent() // Set transparent status bar for call screen
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleArgumentsData() // Get arguments data
        setDataToViews() // Set the arguments to the views (Name, avatar, status etc)
        initiateVoiceCall() // Start the voice call

        // Button to cancel call
        binding.cancelOutgoingCallBtn.setOnClickListener {
            cancelOutgoingCall()
        }
    }

    // Method to reject call
    private fun cancelOutgoingCall() {
        if (sessionID != null) {
            // Reject call with CALL_STATUS_CANCELLED to notify the onIncomingCallCancelled listener
            CometChat.rejectCall(
                sessionID!!,
                CometChatConstants.CALL_STATUS_CANCELLED,
                object : CometChat.CallbackListener<Call>() {
                    override fun onSuccess(p0: Call?) {
                        closeFragment()
                        Log.d(TAG, "Call rejected successfully with status: " + p0?.callStatus)
                    }

                    override fun onError(p0: CometChatException?) {
                        Log.d(TAG, "Call rejection failed with exception: " + p0?.message)
                    }
                })
        }
    }

    // Finish current activity (VoiceCall activity)
    private fun closeFragment() {
        if (activity != null)
            requireActivity().finish()
    }

    // Set the data to te views
    private fun setDataToViews() {
        binding.contactName.text = userName
        Glide.with(this)
            .load(userAvatar)
            .into(binding.avatar)
    }

    // Method to initiates voice call
    private fun initiateVoiceCall() {
        // Check if receiverId is not null before initiating the call
        if (receiverId != null) {
            // Check Receiver type and create Call Object
            val call = if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) Call(
                receiverId!!,
                CometChatConstants.RECEIVER_TYPE_USER,
                CometChatConstants.CALL_TYPE_AUDIO
            ) else Call(
                receiverId!!,
                CometChatConstants.RECEIVER_TYPE_GROUP,
                CometChatConstants.CALL_TYPE_AUDIO
            )

            // Initiate Call with Call object
            CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    sessionID = call.sessionId
                    binding.callDuration.text = "Ringing"
                    Log.d(TAG, "onSuccess initiateCall, Call Type - ${call.receiverType}")

                }

                override fun onError(e: CometChatException) {
                    Log.d(TAG, "Call initialization failed with exception: " + e.message)
                }
            })
        }
    }

    private fun handleArgumentsData() { // Get the arguments data
        if (arguments != null) {
            userName = requireArguments().getString(Constants.USER_NAME)
            userAvatar = requireArguments().getString(Constants.AVATAR)
            receiverId = requireArguments().getString(Constants.RECEIVER_ID)
            receiverType = requireArguments().getString(Constants.RECEIVER_TYPE)
        } else {
            Toast.makeText(requireContext(), "Error Loading Data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun statusBarTransparent() { // Make status bar transparent
        if (activity != null) {
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            requireActivity().window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }
}