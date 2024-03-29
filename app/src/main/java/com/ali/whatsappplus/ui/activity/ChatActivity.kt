package com.ali.whatsappplus.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityChatBinding
import com.bumptech.glide.Glide

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("name")
        val userAvatar = intent.getStringExtra("avatar")
        setUserData(userName, userAvatar)
    }

    private fun setUserData(contactName: String?, userAvatar: String?) {
        if (contactName != null){
            binding.contactName.text = contactName
        }

        if (userAvatar != null){
            Glide.with(this)
                .load(userAvatar)
                .into(binding.profilePic)
        }
    }
}