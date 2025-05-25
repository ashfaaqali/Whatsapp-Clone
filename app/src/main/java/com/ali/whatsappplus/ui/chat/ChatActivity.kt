package com.ali.whatsappplus.ui.chat

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.R
//import com.ali.whatsappplus.data.remote.ApiService
import com.ali.whatsappplus.databinding.ActivityChatBinding
import com.ali.whatsappplus.ui.call.VoiceCall
import com.ali.whatsappplus.ui.chat.bottomsheet.AttachmentsBottomSheet
import com.ali.whatsappplus.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.CallbackListener
import com.cometchat.chat.core.CometChat.endTyping
import com.cometchat.chat.core.CometChat.markAsDelivered
import com.cometchat.chat.core.CometChat.markAsRead
import com.cometchat.chat.core.CometChat.sendMediaMessage
import com.cometchat.chat.core.CometChat.startTyping
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val TAG = "ChatActivity"
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<BaseMessage>()
    private var selectedMessages: List<Int> = emptyList()
    private lateinit var typingIndicator: TypingIndicator
    private var receiverId = ""
    private var receiverType = ""
    private var userName = ""
    private var userAvatar: String? = null
    private var hasNoMoreMessages = false
    private var isInProgress = false
    private lateinit var layoutManager: LinearLayoutManager
    private var messagesRequest: MessagesRequest? = null
    private var latestReceivedMessageId = 0
    private lateinit var bottomSheetDialog: AttachmentsBottomSheet
    private var onImageClicked: OnImageClick? = null

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

    // Typing Indicator To Notify The Other User Of Typing Status
    private fun sendTypingIndicator(isTyping: Boolean) {
        typingIndicator = TypingIndicator(receiverId, CometChatConstants.RECEIVER_TYPE_USER)
        if (isTyping) {
            startTyping(typingIndicator)
        } else {
            endTyping(typingIndicator)
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val file = createFileFromUri(uri)
                Log.i(TAG, file.toString())
                if (file != null) sendMediaMessage(file)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatAdapter = ChatAdapter(this, messageList)
        handleIntentData()
        setUserData()
        setupRecyclerView()
        triggerListeners()

        binding.messageEditText.addTextChangedListener(mTextEditorWatcher)

        onImageClicked = object : OnImageClick {
            override fun onImageClick(
                intent: Intent,
                bundle: Bundle,
                imageUrl: ArrayList<String>,
                itemView: View
            ) {
                setExitSharedElementCallback(object : SharedElementCallback() {
                    override fun onMapSharedElements(
                        names: MutableList<String>?,
                        sharedElements: MutableMap<String, View>?
                    ) {
                        val transitionName = names?.get(0) ?: return
                        val ids = listOf(
                            R.id.left_image_message,
                            R.id.right_image_message,
                        )
                        val views = mutableListOf<ImageView>()
                        ids.forEach {
                            val view =
                                binding.chatMessagesRecyclerView.layoutManager?.findViewByPosition(0)?.findViewById<ImageView>(
                                    it
                                ) ?: return@forEach
                            views.add(view)
                        }
                        val view = views.find { transitionName == it.transitionName } ?: return
                        sharedElements?.put(transitionName, view)
                    }
                })
                startActivity(
                    ImageViewer.createIntent(
                        this@ChatActivity,
                        imageUrl,
                        0
                    ),
                    getActivityOption(itemView).toBundle()
                )
            }
        }
        // Observe The LiveData For Selected Messages
        chatAdapter.getSelectedMessagesLiveData().observe(this) { selectedMessages ->
            toggleToolbar(selectedMessages)
            this.selectedMessages = selectedMessages
        }

        binding.selectedMessagesToolbar.imgDelete.setOnClickListener {
            Log.d(TAG, "onCreate: binding.selectedMessagesToolbar.imgDelete.setOnClickListener")
            if (selectedMessages.isNotEmpty()) {
                Log.d(TAG, "onCreate: selectedMessages.isNotEmpty")

                val messageIds = selectedMessages.toList()
                // Loop To Delete The Selected Messages
                for (messageId in messageIds) {
                    deleteSelectedMessage(messageId)
                }
            }
        }

        binding.voiceRecorderAndSendBtn.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageEditText.setText("")
            }
        }

        binding.attachmentBtn.setOnClickListener {
            showAttachmentsBottomSheet()
        }

        // Initiate Voice Call
        binding.voiceCall.setOnClickListener {
            navigateToVoiceCallActivity(
                userName,
                receiverId,
                userAvatar,
                receiverType,
                Constants.OUTGOING_CALL_FRAGMENT
            )
        }

        binding.videoCall.setOnClickListener {
            navigateToVoiceCallActivity(
                userName,
                receiverId,
                userAvatar,
                receiverType,
                Constants.PRESENTER_FRAGMENT
            )
        }

        binding.back.setOnClickListener {
            finish()
        }
    }

    private fun getActivityOption(targetView: View): ActivityOptionsCompat {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            targetView,
            targetView.transitionName
        )
    }


    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun deleteSelectedMessage(messageId: Int) {
        /*CometChat.deleteMessage(messageId, object : CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                Log.i(TAG, "deleteMessage onSuccess : " + message.deletedAt)
                chatAdapter.removeMessage(message)
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "deleteMessage onError : " + e.message)
            }

        })*/

//        val apiService = ApiService.getInstance()
//        apiService.deleteMessage(messageId.toString(), false, object : ApiService.ApiListener {
//            override fun onResponse(response: String?, error: String?) {
//
//                if (response != null) {
//                    Log.i(TAG, "onResponse: Response --> $response")
//                }
//
//                if (error != null) {
//                    Log.e(TAG, "onResponse: Error --> $error")
//                }
//            }
//
//        })
    }

    private fun toggleToolbar(selectedMessages: List<Int>) {
        if (selectedMessages.isNotEmpty()) {
            binding.toolbar.visibility = View.GONE
            binding.selectedMessagesToolbar.toolbar.visibility = View.VISIBLE
            binding.selectedMessagesToolbar.selectedMessagesCount.text =
                selectedMessages.size.toString()
        } else {
            binding.toolbar.visibility = View.VISIBLE
            binding.selectedMessagesToolbar.toolbar.visibility = View.GONE
        }
    }

    private fun showAttachmentsBottomSheet() {
        bottomSheetDialog = AttachmentsBottomSheet()
        bottomSheetDialog.show(supportFragmentManager, Constants.ATTACHMENTS_BOTTOM_SHEET_TAG)
    }

    // Image Picker Intent
    fun openImagePicker() {
        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }

    // Create File object from URI
    private fun createFileFromUri(uri: Uri): File? {
        val contentResolver = this.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        inputStream?.let { inputStream1 ->
            val file = createImageFile() // Create Image File To Display In The Chat
            try {
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (inputStream1.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                return file
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    private fun fetchMessage() {
        if (messagesRequest == null) {
            messagesRequest = if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                MessagesRequest.MessagesRequestBuilder()
                    .setLimit(10)
                    .setUID(receiverId)
                    .setTypes(arrayListOf("text", "image"))
                    .setCategories(arrayListOf("message"))
                    .build()
            } else {
                MessagesRequest.MessagesRequestBuilder()
                    .setLimit(10)
                    .setGUID(receiverId)
                    .build()
            }
        }
        fetchPreviousMessages()
    }

    private fun fetchPreviousMessages() {
        messagesRequest?.fetchPrevious(object : CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(baseMessages: List<BaseMessage>) {
                handleMessagesFetched(baseMessages)
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "Previous messages fetching failed: ${e.message}")
            }
        })
    }

    private fun fetchNextMessages() {
        latestReceivedMessageId = CometChat.getLastDeliveredMessageId()
        messagesRequest?.fetchNext(object : CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(baseMessages: List<BaseMessage>) {
                handleMessagesFetched(baseMessages)
            }

            override fun onError(e: CometChatException) {
                Log.e(
                    TAG,
                    "Next messages fetching failed: ${e.message}. Last received message ID: $latestReceivedMessageId"
                )
            }
        })
    }

    private fun handleMessagesFetched(baseMessages: List<BaseMessage>) {
        if (baseMessages.isNotEmpty()) {
            baseMessages.forEach { message ->
                if (message is TextMessage)
                    Log.i(TAG, "Message fetched: ${message.id} readAt: ${message.readAt} text: ${message.text}")
            }

            val lastMessage = baseMessages.last()
            markAsRead(lastMessage)
            chatAdapter.updateList(baseMessages)

            // Scroll to the new messages
            scrollToNewMessages(baseMessages.size)
        } else {
            hasNoMoreMessages = true
        }
        isInProgress = false
    }

    private fun scrollToNewMessages(itemsAdded: Int) {
        if (itemsAdded > 0) {
            binding.chatMessagesRecyclerView.scrollToPosition(itemsAdded)
        }
    }

    private fun sendMessage(message: String) {
        val textMessage = TextMessage(receiverId, message, receiverType)
        // textMessage.tags = mutableListOf("hero", "coco")
        CometChat.sendMessage(textMessage, object : CallbackListener<TextMessage>() {
            override fun onSuccess(message: TextMessage) {
                addMessageToChat(message)
                Log.i(TAG, "sendMessage onSuccess: ID ${message.id}")
                Log.i(TAG, "sendMessage onSuccess: readAt -> ${message.readAt}")
                Log.i(TAG, "sendMessage onSuccess: text -> ${message.text}")
            }

            override fun onError(p0: CometChatException?) {
                Log.e(TAG, "sendMessage onError: ${p0?.message}")
            }
        })
    }

    private fun addMessageToChat(message: BaseMessage) {
        chatAdapter.addMessage(message)
        binding.chatMessagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun sendMediaMessage(file: File) {
        val mediaMessage =
            if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) MediaMessage(
                receiverId,
                file,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.RECEIVER_TYPE_USER
            ) else MediaMessage(
                receiverId,
                file,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.RECEIVER_TYPE_GROUP
            )

        sendMediaMessage(mediaMessage, object : CallbackListener<MediaMessage>() {
            override fun onSuccess(message: MediaMessage) {
                addMessageToChat(message)
                Log.i(TAG, "sendMediaMessage OnSuccess: $message")
            }

            override fun onError(p0: CometChatException?) {
                Log.e(TAG, "sendMediaMessage OnError: $p0")
            }

        })
    }

    private fun triggerListeners() {
        CometChat.addMessageListener(TAG, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(textMessage: TextMessage?) {
                Log.i(TAG, "onTextMessageReceived: $textMessage")
                if (textMessage != null) {
                    markMessageAsDelivered(textMessage)
                    markMessageAsRead(textMessage)
                    addMessageToChat(textMessage)
                }
            }

            override fun onMediaMessageReceived(mediaMessage: MediaMessage?) {
                Log.i(TAG, "onMediaMessageReceived: ${mediaMessage?.file}")
                if (mediaMessage != null) {
                    markMessageAsDelivered(mediaMessage)
                    markMessageAsRead(mediaMessage)
                    addMessageToChat(mediaMessage)
                }
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

            override fun onMessagesDelivered(messageDeliveryReceipt: MessageReceipt) {
                Log.d(TAG, "onMessageDelivered: $messageDeliveryReceipt")
            }

            override fun onMessagesRead(messageReadReceipt: MessageReceipt?) {
                Log.d(TAG, "onMessagesRead: $messageReadReceipt")
            }
        })
    }

    private fun markMessageAsRead(baseMessage: BaseMessage) {
        markAsRead(
            baseMessage.id,
            baseMessage.receiverUid,
            baseMessage.receiverType,
            baseMessage.sender.uid,
            object : CallbackListener<Void?>() {
                override fun onSuccess(unused: Void?) {
                    Log.i(
                        TAG,
                        "markAsRead onSuccess : messageId : ${baseMessage.id} readAt : ${baseMessage.deliveredAt}"
                    )
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "markAsRead onError : " + e.message)
                }
            })
    }

    private fun markMessageAsDelivered(baseMessage: BaseMessage) {
        markAsDelivered(
            baseMessage.id,
            baseMessage.receiverUid,
            baseMessage.receiverType,
            baseMessage.sender.uid,
            object : CallbackListener<Void?>() {
                override fun onSuccess(unused: Void?) {
                    Log.i(
                        TAG,
                        "markAsDelivered onSuccess : messageId : ${baseMessage.id} deliveredAt : ${baseMessage.deliveredAt}"
                    )
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "markAsDelivered onError : " + e.message)
                }
            })
    }

    private fun isUser(): Boolean {
        return receiverType == "user"
    }

    private fun fetchMessagesHistory() {

    }

    private fun handleIntentData() {
        if (intent != null) {
            userName = intent.getStringExtra(Constants.USER_NAME).toString()
            userAvatar = intent.getStringExtra(Constants.AVATAR)
            receiverId = intent.getStringExtra(Constants.RECEIVER_ID).toString()
            receiverType = intent.getStringExtra(Constants.RECEIVER_TYPE).toString()
        } else {
            Toast.makeText(applicationContext, "Error Loading Data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatMessagesRecyclerView.layoutManager = layoutManager
        binding.chatMessagesRecyclerView.adapter = chatAdapter

        // Add scroll listener to RecyclerView to fetch next next list of messages.
        binding.chatMessagesRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!hasNoMoreMessages && !isInProgress) {
                    if (!binding.chatMessagesRecyclerView.canScrollVertically(
                            -1
                        )
                    ) {
                        isInProgress = true
                        fetchMessage()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchMessage()
    }

    override fun onPause() {
        super.onPause()
        // Removing the message listener when not in use.
        CometChat.removeMessageListener(TAG)
    }

    // Set user data (Profile picture, name etc)
    private fun setUserData() {
        binding.contactName.text = userName
        if (userAvatar != null) {
            Glide.with(this)
                .load(userAvatar)
                .into(binding.profilePic)
        } else {
            if (receiverType == CometChatConstants.RECEIVER_TYPE_USER) binding.profilePic.setImageResource(
                R.drawable.ic_user_profile
            )
            else {
                binding.profilePic.setImageResource(R.drawable.ic_group_profile)
                binding.profilePic.setPadding(20, 20, 20, 20)
                binding.profilePic.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                binding.profilePic.background =
                    ContextCompat.getDrawable(this, R.drawable.circular_bg)
                binding.profilePic.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey_shade))
            }
        }
    }

    private fun navigateToVoiceCallActivity(
        name: String,
        uid: String,
        avatar: String?,
        receiverType: String,
        fragmentToLoad: String
    ) {
        val intent = Intent(this, VoiceCall::class.java)
        intent.putExtra(Constants.USER_NAME, name)
        intent.putExtra(Constants.RECEIVER_ID, uid)
        intent.putExtra(Constants.AVATAR, avatar)
        intent.putExtra(Constants.RECEIVER_TYPE, receiverType)
        intent.putExtra(Constants.FRAGMENT_TO_LOAD, fragmentToLoad)
        startActivity(intent)
    }
}