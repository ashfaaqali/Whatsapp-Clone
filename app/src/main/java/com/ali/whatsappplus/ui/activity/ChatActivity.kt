package com.ali.whatsappplus.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityChatBinding
import com.ali.whatsappplus.ui.adapter.ChatAdapter
import com.ali.whatsappplus.viewmodel.ChatViewModel
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.CallbackListener
import com.cometchat.chat.core.CometChat.endTyping
import com.cometchat.chat.core.CometChat.startTyping
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val TAG = "ChatActivity"
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<TextMessage>()
    private lateinit var typingIndicator: TypingIndicator
    private var receiverId = ""
    private var receiverType = ""
    private var userName = ""
    private var userAvatar = ""

    private lateinit var messagesRequest: MessagesRequest
    private var latestReceivedMessageId = 0

    private val mTextEditorWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.isNotEmpty()) {
                sendTypingIndicator(isTyping = true)
            } else {
                sendTypingIndicator(isTyping = false)
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isNotEmpty()) {
                binding.voiceRecorderAndSendBtn.setImageResource(R.drawable.ic_send)
            } else {
                binding.voiceRecorderAndSendBtn.setImageResource(R.drawable.ic_mic)
            }
        }
    }

    private fun sendTypingIndicator(isTyping: Boolean) {
        typingIndicator = TypingIndicator(receiverId, CometChatConstants.RECEIVER_TYPE_USER)
        if (isTyping) {
            startTyping(typingIndicator)

        } else {
            endTyping(typingIndicator)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatViewModel = ChatViewModel()
        chatAdapter = ChatAdapter(messageList)

        handleIntentData()
        setUserData()
        setupRecyclerView()
        messageListener()
        fetchMissedMessages()
        fetchUnreadMessages()

        binding.messageEditText.addTextChangedListener(mTextEditorWatcher)

        binding.voiceRecorderAndSendBtn.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            sendMessage(message)
            binding.messageEditText.setText("")
        }
    }

    private fun fetchMissedMessages() {
        latestReceivedMessageId = CometChat.getLastDeliveredMessageId()
        Log.d(TAG, "Latest Message ID: $latestReceivedMessageId")

        messagesRequest = MessagesRequest.MessagesRequestBuilder()
            .setMessageId(latestReceivedMessageId)
            .setLimit(20)
            .setUID(receiverId)
            .build()

        Log.d(TAG, "Message Request: $messagesRequest")

        messagesRequest.fetchNext(object : CallbackListener<List<BaseMessage?>>() {
            override fun onSuccess(list: List<BaseMessage?>) {
                for (message in list) {
                    if (message is TextMessage) {
                        Log.d(TAG, "Missed Text message received successfully: $message")
                        messageList.add(message)
                        binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount)
                        chatAdapter.notifyDataSetChanged()
                    } else if (message is MediaMessage) {
                        Log.d(TAG, "Missed Media message received successfully: $message")
                    }
                }
            }

            override fun onError(e: CometChatException) {
                Log.d(
                    TAG,
                    "Missed Message fetching failed with exception: " + e.message + latestReceivedMessageId
                )
            }
        })
    }

    private fun messageDelivery(textMessage: TextMessage) {
        CometChat.markAsDelivered(textMessage.id, textMessage.receiverUid, textMessage.receiverType, textMessage.sender.uid, object : CallbackListener<Void?>() {
            override fun onSuccess(unused: Void?) {
                Log.e(TAG, "markAsDelivered Success [$textMessage]")
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "markAsDelivered : " + e.message)
            }
        })
    }

    private fun messageListener() {
        CometChat.addMessageListener("Listener 1", object : CometChat.MessageListener() {
            override fun onTypingEnded(typingIndicator: TypingIndicator?) {
                Log.d(TAG, "onTypingEnded: ${typingIndicator.toString()}")
                binding.status.text = "online"
            }

            override fun onTypingStarted(typingIndicator: TypingIndicator?) {
                Log.d(TAG, "onTypingStarted: ${typingIndicator.toString()}")
                binding.status.text = "Typing..."
            }

            override fun onMessagesDelivered(messageReceipt: MessageReceipt) {

            }

            override fun onMessagesRead(messageReceipt: MessageReceipt) {
                Log.e(TAG, "onMessagesRead: $messageReceipt")
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

    private fun setupRecyclerView() {
        binding.chatMessagesRecyclerView.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatMessagesRecyclerView.layoutManager = layoutManager
    }

    private fun sendMessage(message: String) {
        val receiverType =
            if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) CometChatConstants.RECEIVER_TYPE_USER else CometChatConstants.RECEIVER_TYPE_GROUP
        val textMessage = TextMessage(receiverId, message, receiverType)
        messageList.add(textMessage)
        binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount)
        chatAdapter.notifyDataSetChanged()
        textMessage.sender = CometChat.getLoggedInUser();
        textMessage.receiverUid
        CometChat.sendMessage(textMessage, object : CallbackListener<TextMessage>() {
            override fun onSuccess(message: TextMessage) {
                Log.i(TAG, "Message Sent Success: $message")
                messageDelivery(message)
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
                messageDelivery(textMessage)
                messageList.add(textMessage)
                binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount)
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

    private fun setUserData() {
        binding.contactName.text = userName
        Glide.with(this)
            .load(userAvatar)
            .into(binding.profilePic)
    }

    private fun fetchUnreadMessages(){
        messagesRequest = if (receiverType == CometChatConstants.RECEIVER_TYPE_USER){
            MessagesRequest.MessagesRequestBuilder()
                .setUnread(true)
                .setLimit(20)
                .setUID(receiverId)
                .build()
        } else {
            MessagesRequest.MessagesRequestBuilder()
                .setUnread(true)
                .setLimit(20)
                .setGUID(receiverId)
                .build()
        }

        messagesRequest.fetchPrevious(object : CallbackListener<List<BaseMessage?>>() {
            override fun onSuccess(list: List<BaseMessage?>) {
                for (message in list) {
                    if (message is TextMessage) {
                        Log.d(TAG, "Text message received successfully: $message")
                        messageList.add(message)
                        binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount)
                        chatAdapter.notifyDataSetChanged()
                    } else if (message is MediaMessage) {
                        Log.d(TAG, "Media message received successfully: $message")
                    }
                }
            }

            override fun onError(e: CometChatException) {
                Log.d(TAG, "Message fetching failed with exception: " + e.message)
            }
        })
    }
}