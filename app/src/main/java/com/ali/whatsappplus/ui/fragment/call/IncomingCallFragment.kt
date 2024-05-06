package com.ali.whatsappplus.ui.fragment.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ali.whatsappplus.databinding.FragmentIncomingCallBinding
import com.ali.whatsappplus.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.CallListener

class IncomingCallFragment : Fragment() {

    private lateinit var binding: FragmentIncomingCallBinding
    private var receiverId: String? = null
    private var receiverType: String? = null
    private var userName: String? = null
    private var userAvatar: String? = null
    private val listenerId = "call_listener"
    private val TAG = "IncomingCallFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIncomingCallBinding.inflate(layoutInflater)
        statusBarTransparent() // Set transparent status bar for call screen
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleArgumentsData() // Get arguments data
        setDataToViews() // Set the data to the views (Name, avatar etc.)
    }

    override fun onResume() {
        super.onResume()
        incomingCallListener() // Call Listener
    }

    private fun incomingCallListener() {
        CometChat.addCallListener(listenerId, object : CallListener() {
            override fun onIncomingCallReceived(p0: Call?) {}
            override fun onOutgoingCallAccepted(p0: Call?) {}
            override fun onOutgoingCallRejected(p0: Call?) {}

            override fun onIncomingCallCancelled(p0: Call?) {
                closeFragment()
            }

        })
    }

    // Method to finish current activity (VoiceCall activity)
    private fun closeFragment() {
        if (activity != null)
            requireActivity().finish()
    }

    // Get arguments data
    private fun handleArgumentsData() {
        if (arguments != null) {
            userName = requireArguments().getString(Constants.USER_NAME)
            userAvatar = requireArguments().getString(Constants.AVATAR)
            receiverId = requireArguments().getString(Constants.RECEIVER_ID)
            receiverType = requireArguments().getString(Constants.RECEIVER_TYPE)
        } else {
            Toast.makeText(requireContext(), "Error Loading Data", Toast.LENGTH_SHORT).show()
        }
    }

    // Make status bar transparent
    private fun statusBarTransparent() {
        if (activity != null) {
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            requireActivity().window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    private fun setDataToViews() {
        with(binding) {
            callerName.text = userName
            Glide.with(binding.root)
                .load(userAvatar)
                .into(callerProfilePic)
        }
    }
}