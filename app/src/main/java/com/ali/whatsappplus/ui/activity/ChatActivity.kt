package com.ali.whatsappplus.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ali.whatsappplus.data.model.AllContacts
import com.ali.whatsappplus.databinding.ActivityChatBinding
import com.ali.whatsappplus.ui.adapter.AllContactsAdapter

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val contactName = intent.getStringExtra("name")
        setContactName(contactName)
    }

    private fun setContactName(contactName: String?) {
        if (contactName != null){
            binding.contactName.text = contactName
        }
    }
}