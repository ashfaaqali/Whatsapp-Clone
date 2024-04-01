package com.ali.whatsappplus.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityChatBinding
import com.ali.whatsappplus.ui.adapter.ChatAdapter
import com.ali.whatsappplus.util.Constants
import com.ali.whatsappplus.viewmodel.ChatViewModel
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.CallbackListener
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val TAG = "ChatActivity"
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter

    private val mTextEditorWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isNotEmpty()){
                binding.voiceRecorderAndSendBtn.setImageResource(R.drawable.ic_send)
            } else {
                binding.voiceRecorderAndSendBtn.setImageResource(R.drawable.ic_mic)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatViewModel = ChatViewModel()
        chatAdapter = ChatAdapter(chatViewModel.messageList)

        val userName = intent.getStringExtra("name")
        val userAvatar = intent.getStringExtra("avatar")
        val receiverId = intent.getStringExtra("receiverId")
        setUserData(userName, userAvatar)

        setupRecyclerView()

        binding.messageEditText.addTextChangedListener(mTextEditorWatcher)

        binding.voiceRecorderAndSendBtn.setOnClickListener{
            val message = binding.messageEditText.text.toString()
            if (receiverId != null){
                sendMessage(message, receiverId)
            }
            binding.messageEditText.setText("")
        }
    }

    private fun setupRecyclerView() {
        binding.chatMessagesRecyclerView.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatMessagesRecyclerView.layoutManager = layoutManager
    }

    private fun sendMessage(message: String, receiverId: String) {
        val receiverType = CometChatConstants.RECEIVER_TYPE_USER
        val textMessage = TextMessage(receiverId, message, receiverType)
        CometChat.sendMessage(textMessage, object : CallbackListener<TextMessage>() {
            override fun onSuccess(message: TextMessage?) {
                Log.i(TAG, "Message Sent Success: $message")
                if (message != null) {
                    chatViewModel.addToChat(message.text, Constants.SENT)
                }
                binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount)
                chatAdapter.notifyDataSetChanged()
            }
            override fun onError(p0: CometChatException?) {
                Log.i(TAG, "Message Sent Failed: $p0 [$receiverType] [$receiverId]")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        realTimeMessageListener()
    }

    override fun onPause() {
        super.onPause()
        CometChat.removeMessageListener(TAG)
    }

    private fun realTimeMessageListener() {
        val listenerID = TAG
        CometChat.addMessageListener(listenerID, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(textMessage: TextMessage) {
                Log.d(TAG, "Text message received successfully: $textMessage")
                chatViewModel.addResponse(textMessage.text)
                chatAdapter.notifyDataSetChanged()

            }

            override fun onMediaMessageReceived(mediaMessage: MediaMessage) {
                Log.d(TAG, "Media message received successfully: $mediaMessage")
            }

            override fun onCustomMessageReceived(customMessage: CustomMessage) {
                Log.d(TAG, "Custom message received successfully: $customMessage")
            }
        })
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