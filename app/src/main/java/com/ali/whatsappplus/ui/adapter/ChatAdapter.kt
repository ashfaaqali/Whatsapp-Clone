package com.ali.whatsappplus.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.R.*
import com.ali.whatsappplus.R.drawable.*
import com.ali.whatsappplus.databinding.ChatItemBinding
import com.ali.whatsappplus.databinding.LeftChatImageViewBinding
import com.ali.whatsappplus.databinding.LeftChatTextViewBinding
import com.ali.whatsappplus.databinding.RightChatImageViewBinding
import com.ali.whatsappplus.databinding.RightChatTextViewBinding
import com.ali.whatsappplus.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(val context: Context, baseMessages: List<BaseMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messageList: MutableList<BaseMessage> = ArrayList()
    private lateinit var binding: ChatItemBinding
    private lateinit var message: BaseMessage
    private val TAG = "ChatAdapter"

    init {
        setMessageList(baseMessages)
    }

    private fun setMessageList(messageList: List<BaseMessage>) {
        Log.d(TAG, "setMessagesList called: $messageList")
        this.messageList.addAll(0, messageList)
        notifyItemRangeInserted(0, messageList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constants.LEFT_CHAT_TEXT_VIEW -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: LeftChatTextViewBinding =
                    LeftChatTextViewBinding.inflate(layoutInflater, parent, false)
                textMessageItemBinding.root.tag = Constants.LEFT_CHAT_TEXT_VIEW
                LeftChatTextView(textMessageItemBinding)
            }

            Constants.RIGHT_CHAT_TEXT_VIEW -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: RightChatTextViewBinding =
                    RightChatTextViewBinding.inflate(layoutInflater, parent, false)
                textMessageItemBinding.root.tag = Constants.RIGHT_CHAT_TEXT_VIEW
                RightChatTextView(textMessageItemBinding)
            }

            Constants.RIGHT_CHAT_IMAGE_VIEW -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val imageMessageItemBinding: RightChatImageViewBinding =
                    RightChatImageViewBinding.inflate(layoutInflater, parent, false)
                imageMessageItemBinding.root.tag = Constants.RIGHT_CHAT_IMAGE_VIEW
                RightChatImageView(imageMessageItemBinding)
            }

            Constants.LEFT_CHAT_IMAGE_VIEW -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val imageMessageItemBinding: LeftChatImageViewBinding =
                    LeftChatImageViewBinding.inflate(layoutInflater, parent, false)
                imageMessageItemBinding.root.tag = Constants.LEFT_CHAT_IMAGE_VIEW
                LeftChatImageView(imageMessageItemBinding)
            }

            else -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val imageMessageItemBinding: LeftChatImageViewBinding =
                    LeftChatImageViewBinding.inflate(layoutInflater, parent, false)
                imageMessageItemBinding.root.tag = Constants.LEFT_CHAT_IMAGE_VIEW
                LeftChatImageView(imageMessageItemBinding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val baseMessage = messageList[position]

        val loggedInUser = CometChat.getLoggedInUser().uid
        val sender = baseMessage.sender.uid

        return when (baseMessage.type) {
            CometChatConstants.MESSAGE_TYPE_TEXT -> {
                if (sender == loggedInUser) Constants.RIGHT_CHAT_TEXT_VIEW
                else Constants.LEFT_CHAT_TEXT_VIEW
            }

            CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                if (sender == loggedInUser) Constants.RIGHT_CHAT_IMAGE_VIEW
                else Constants.LEFT_CHAT_IMAGE_VIEW
            }

            else -> -1
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val baseMessage = messageList[position]

        when (viewHolder.itemViewType) {
            Constants.LEFT_CHAT_TEXT_VIEW -> setTextData(viewHolder as LeftChatTextView, position)
            Constants.RIGHT_CHAT_TEXT_VIEW -> setTextData(viewHolder as RightChatTextView, position)
            Constants.LEFT_CHAT_IMAGE_VIEW -> setImageData(viewHolder as LeftChatImageView, position)
            Constants.RIGHT_CHAT_IMAGE_VIEW -> setImageData(viewHolder as RightChatImageView, position)
        }
//        setStatusIcon(baseMessage)
    }

    private fun setImageData(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val mediaMessage = (baseMessage as MediaMessage)
        Log.i(TAG, "File URL: ${mediaMessage.attachment.fileUrl}")
        if (viewHolder is LeftChatImageView){
            Glide.with(context)
                .load(mediaMessage.attachment.fileUrl)
                .into(viewHolder.binding.leftImageMessage)
        } else {
            viewHolder as RightChatImageView
            Glide.with(context)
                .load(mediaMessage.attachment.fileUrl)
                .into(viewHolder.binding.rightImageMessage)
        }
    }

    private fun setTextData(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val textMessage = (baseMessage as TextMessage)

        if (viewHolder is LeftChatTextView) {
            viewHolder.binding.leftMessageTextView.text = textMessage.text
        } else {
            viewHolder as RightChatTextView
            viewHolder.binding.rightChatMessageTxtView.text = textMessage.text
        }
    }

    fun updateList(baseMessageList: List<BaseMessage>) {
        setMessageList(baseMessageList)
    }

    private fun getTimestamp(milliSeconds: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = Date(milliSeconds)
        return sdf.format(date)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun addMessage(baseMessage: BaseMessage) {
        messageList.add(baseMessage)
        notifyItemInserted(messageList.size - 1)
    }

    private fun setStatusIcon(baseMessage: BaseMessage) {
        with(binding) {
            if (baseMessage.readAt > 0) {
                messageStatusIcon.setImageResource(ic_delivered)
                val blueColor = ContextCompat.getColorStateList(context, color.blue)
                messageStatusIcon.imageTintList = blueColor
            } else if (baseMessage.deliveredAt > 0) {
                messageStatusIcon.setImageResource(ic_delivered)
                val deliveredColor =
                    ContextCompat.getColorStateList(context, color.grey)
                messageStatusIcon.imageTintList = deliveredColor
            } else {
                rightChatTimestampTxtView.text = baseMessage.sentAt.toString()
                messageStatusIcon.setImageResource(ic_sent)
                val greyColor =
                    ContextCompat.getColorStateList(context, color.grey)
                messageStatusIcon.imageTintList = greyColor
            }
        }
    }

    inner class LeftChatTextView(val binding: LeftChatTextViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RightChatTextView(val binding: RightChatTextViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class LeftChatImageView(val binding: LeftChatImageViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RightChatImageView(val binding: RightChatImageViewBinding) :
        RecyclerView.ViewHolder(binding.root)
}