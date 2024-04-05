package com.ali.whatsappplus.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityChatBinding
import com.ali.whatsappplus.ui.adapter.ChatAdapter
import com.ali.whatsappplus.viewmodel.ChatViewModel
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.CallbackListener
import com.cometchat.chat.core.CometChat.endTyping
import com.cometchat.chat.core.CometChat.markAsRead
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
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<BaseMessage>()
    private lateinit var typingIndicator: TypingIndicator
    private var receiverId = ""
    private var receiverType = ""
    private var userName = ""
    private var userAvatar = ""
    private var hasNoMoreMessages = false
    private var isInProgress = false
    private lateinit var layoutManager: LinearLayoutManager

    private var messagesRequest: MessagesRequest? = null
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
        chatAdapter = ChatAdapter(messageList)

        handleIntentData()
        setUserData()
        setupRecyclerView()
//        fetchMissedMessages()
//        fetchUnreadMessages()
        fetchMessage()

        binding.messageEditText.addTextChangedListener(mTextEditorWatcher)

        binding.voiceRecorderAndSendBtn.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            sendMessage(message)
            binding.messageEditText.setText("")
        }
    }

    private fun fetchMessage() {

        if (messagesRequest == null) {
            messagesRequest = if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                MessagesRequest.MessagesRequestBuilder()
                    .setLimit(30)
                    .setUID(receiverId)
                    .build()
            } else {
                MessagesRequest.MessagesRequestBuilder()
                    .setLimit(30)
                    .setGUID(receiverId)
                    .build()
            }
        }

        messagesRequest?.fetchPrevious(object : CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(baseMessages: List<BaseMessage>) {
                for (message in baseMessages) {
                    if (message is TextMessage) {
                        Log.d(TAG, "Message history fetched successfully: $message")
                    } else if (message is MediaMessage) {
                        Log.d(TAG, "Media message history fetched successfully: $message")
                    }
                }

                isInProgress = false
                chatAdapter.updateList(baseMessages)
                // Calculate the number of items added

                val itemsAdded = baseMessages.size

                if (itemsAdded > 0) {
                    binding.chatMessagesRecyclerView.scrollToPosition(itemsAdded)
                }

                if (baseMessages.isNotEmpty()) {
                    val baseMessage = baseMessages[baseMessages.size - 1]
                    markAsRead(baseMessage)
                }

                if (baseMessages.isEmpty()) {
                    hasNoMoreMessages = true
                }
            }

            override fun onError(e: CometChatException) {
                Log.d(TAG, "Message fetching failed with exception: " + e.message)
            }
        })
    }

    private fun sendMessage(message: String) {
        val receiverType =
            if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) CometChatConstants.RECEIVER_TYPE_USER else CometChatConstants.RECEIVER_TYPE_GROUP
        val textMessage = TextMessage(receiverId, message, receiverType)
        chatAdapter.addMessage(textMessage)
        binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
        textMessage.sender = CometChat.getLoggedInUser()
        CometChat.sendMessage(textMessage, object : CallbackListener<TextMessage>() {
            override fun onSuccess(message: TextMessage) {
                Log.i(TAG, "Message Sent Success: $message")
                chatAdapter.updateSentMessage(message)
            }

            override fun onError(p0: CometChatException?) {
                Log.i(TAG, "Message Sent Failed: $p0 [$receiverType] [$receiverId]")
            }
        })
    }

    private fun messageListener() {
        CometChat.addMessageListener(TAG, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(textMessage: TextMessage?) {
                if (textMessage != null) {
                    chatAdapter.addMessage(textMessage)
                    binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }
            }

            override fun onMediaMessageReceived(p0: MediaMessage?) {

            }

            override fun onCustomMessageReceived(p0: CustomMessage?) {

            }

            override fun onTypingEnded(typingIndicator: TypingIndicator?) {
                Log.d(TAG, "onTypingEnded: ${typingIndicator.toString()}")
                binding.status.text = "online"
            }

            override fun onTypingStarted(typingIndicator: TypingIndicator?) {
                Log.d(TAG, "onTypingStarted: ${typingIndicator.toString()}")
                binding.status.text = "Typing..."
            }

            override fun onMessagesDelivered(messageDeliveryReceipt: MessageReceipt?) {

            }

            override fun onMessagesRead(messageReadReceipt: MessageReceipt?) {

            }

        })
    }

    private fun fetchMissedMessages() {
        latestReceivedMessageId = CometChat.getLastDeliveredMessageId()
        Log.d(TAG, "Latest Message ID: $latestReceivedMessageId")

        messagesRequest?.fetchNext(object : CallbackListener<List<BaseMessage?>>() {
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
        CometChat.markAsDelivered(
            textMessage.id,
            textMessage.receiverUid,
            textMessage.receiverType,
            textMessage.sender.uid,
            object : CallbackListener<Void?>() {
                override fun onSuccess(unused: Void?) {
                    Log.e(TAG, "markAsDelivered Success [$textMessage]")
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "markAsDelivered : " + e.message)
                }
            }
        )
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
        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatMessagesRecyclerView.layoutManager = layoutManager
        binding.chatMessagesRecyclerView.adapter = chatAdapter

        // Add scroll listener to RecyclerView
        binding.chatMessagesRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!hasNoMoreMessages && !isInProgress) {
                    if (layoutManager.findFirstVisibleItemPosition() == 10 || !binding.chatMessagesRecyclerView.canScrollVertically(-1)) {
                        isInProgress = true
                        fetchMessage()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        messageListener()
    }

    override fun onPause() {
        super.onPause()
        CometChat.removeMessageListener(TAG)
    }

    private fun setUserData() {
        binding.contactName.text = userName
        Glide.with(this)
            .load(userAvatar)
            .into(binding.profilePic)
    }
}