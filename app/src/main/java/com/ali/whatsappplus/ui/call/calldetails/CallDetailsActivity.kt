package com.ali.whatsappplus.ui.call.calldetails

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityCallDetailsBinding
import com.ali.whatsappplus.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants

class CallDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallDetailsBinding
    private var userName: String? = null
    private var userAvatar: String? = null
    private var receiverId: String? = null
    private var receiverType: String? = null

    private val TAG = "CallDetailsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIntentData()
        setDataToViews()
    }

    // Set user data (Profile picture, name etc)
    private fun setDataToViews() {
        binding.callerName.text = userName
        Log.d(TAG, "setDataToViews: $userName, ${binding.callerName.text}")
        if (userAvatar != null) {
            Glide.with(this)
                .load(userAvatar)
                .into(binding.callerProfilePic)
        } else if (receiverType != null) {
            if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) binding.callerProfilePic.setImageResource(
                R.drawable.ic_user_profile
            )
            else {
                binding.callerProfilePic.setImageResource(R.drawable.ic_group_profile)
                binding.callerProfilePic.setPadding(20, 20, 20, 20)
                binding.callerProfilePic.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                binding.callerProfilePic.background =
                    ContextCompat.getDrawable(this, R.drawable.circular_bg)
                binding.callerProfilePic.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey_shade))
            }

        }


    }

    // Get intent data
    private fun handleIntentData() {
        if (intent != null) {
            userName = intent.getStringExtra(Constants.USER_NAME)
            userAvatar = intent.getStringExtra("avatar")
            receiverId = intent.getStringExtra("receiverId")
            receiverType = intent.getStringExtra("receiverType")
            Log.d(
                TAG,
                "Name: $userName, Avatar: $userAvatar, ReceiverId: $receiverId, ReceiverType: $receiverType"
            )
        } else {
            Toast.makeText(applicationContext, "Error Loading Data", Toast.LENGTH_SHORT).show()
        }


    }
}