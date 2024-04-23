package com.ali.whatsappplus.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ali.whatsappplus.R
import com.ali.whatsappplus.R.color
import com.ali.whatsappplus.R.drawable.ic_delivered
import com.ali.whatsappplus.R.drawable.ic_sent
import com.ali.whatsappplus.databinding.LeftChatImageViewBinding
import com.ali.whatsappplus.databinding.LeftChatTextViewLongBinding
import com.ali.whatsappplus.databinding.LeftChatTextViewShortBinding
import com.ali.whatsappplus.databinding.RightChatImageViewBinding
import com.ali.whatsappplus.databinding.RightChatTextViewLongBinding
import com.ali.whatsappplus.databinding.RightChatTextViewShortBinding
import com.ali.whatsappplus.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(val context: Context, baseMessages: List<BaseMessage>) :
    RecyclerView.Adapter<ViewHolder>() {
    private var messageList: MutableList<BaseMessage> = ArrayList()
    private val selectedMessages: MutableList<Int> = mutableListOf()
    private val TAG = "ChatAdapter"

    init {
        setMessageList(baseMessages)
    }

    // Setting messages list to the recycler view
    private fun setMessageList(messageList: List<BaseMessage>) {
        this.messageList.addAll(0, messageList)
        notifyItemRangeInserted(0, messageList.size)
    }

    override fun getItemViewType(position: Int): Int {
        val baseMessage = messageList[position]
        val loggedInUser = CometChat.getLoggedInUser().uid
        val sender = baseMessage.sender.uid

        // Checking and returning view type to OnCreateViewHolder
        return if (baseMessage is TextMessage) {
            // Two different views are being returned here according to the message length just like in WhatsApp.
            if (sender == loggedInUser) {
                // If message length is greater than 30 return a layout with timestamp and status icon below the message.
                if (baseMessage.text.length > 30) Constants.RIGHT_CHAT_TEXT_VIEW_LONG
                // else return a layout with timestamp and status icon next to the message.
                else Constants.RIGHT_CHAT_TEXT_VIEW_SHORT
            } else {
                // Same thing on left side, Timestamp and status icon below the message.
                if (baseMessage.text.length > 30) Constants.LEFT_CHAT_TEXT_VIEW_LONG
                // Timestamp and status icon next to the message.
                else Constants.LEFT_CHAT_TEXT_VIEW_SHORT
            }
        } else if (baseMessage is MediaMessage) {
            if (sender == loggedInUser) Constants.RIGHT_CHAT_IMAGE_VIEW
            else Constants.LEFT_CHAT_IMAGE_VIEW
        } else -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Returning the appropriate view according to the ItemViewType
        return when (viewType) {
            Constants.LEFT_CHAT_TEXT_VIEW_LONG -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: LeftChatTextViewLongBinding =
                    LeftChatTextViewLongBinding.inflate(layoutInflater, parent, false)
                textMessageItemBinding.root.tag = Constants.LEFT_CHAT_TEXT_VIEW_LONG
                LeftChatTextViewLong(textMessageItemBinding)
            }

            Constants.LEFT_CHAT_TEXT_VIEW_SHORT -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: LeftChatTextViewShortBinding =
                    LeftChatTextViewShortBinding.inflate(layoutInflater, parent, false)
                textMessageItemBinding.root.tag = Constants.LEFT_CHAT_TEXT_VIEW_SHORT
                LeftChatTextViewShort(textMessageItemBinding)
            }

            Constants.RIGHT_CHAT_TEXT_VIEW_LONG -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: RightChatTextViewLongBinding =
                    RightChatTextViewLongBinding.inflate(layoutInflater, parent, false)
                textMessageItemBinding.root.tag = Constants.RIGHT_CHAT_TEXT_VIEW_LONG
                RightChatTextViewLong(textMessageItemBinding)
            }

            Constants.RIGHT_CHAT_TEXT_VIEW_SHORT -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val textMessageItemBinding: RightChatTextViewShortBinding =
                    RightChatTextViewShortBinding.inflate(layoutInflater, parent, false)
                textMessageItemBinding.root.tag = Constants.RIGHT_CHAT_TEXT_VIEW_SHORT
                RightChatTextViewShort(textMessageItemBinding)
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

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val messageId = baseMessage.id

        // Setting the data to the views according to the view type
        when (viewHolder.itemViewType) {
            Constants.LEFT_CHAT_TEXT_VIEW_LONG -> setTextData(
                viewHolder as LeftChatTextViewLong,
                position
            )

            Constants.LEFT_CHAT_TEXT_VIEW_SHORT -> {
                setTextData(
                    viewHolder as LeftChatTextViewShort,
                    position
                )
            }

            Constants.RIGHT_CHAT_TEXT_VIEW_LONG -> {
                setTextData(
                    viewHolder as RightChatTextViewLong,
                    position
                )
            }

            Constants.RIGHT_CHAT_TEXT_VIEW_SHORT -> setTextData(
                viewHolder as RightChatTextViewShort,
                position
            )

            Constants.LEFT_CHAT_IMAGE_VIEW -> setImageData(
                viewHolder as LeftChatImageView,
                position
            )

            Constants.RIGHT_CHAT_IMAGE_VIEW -> setImageData(
                viewHolder as RightChatImageView,
                position
            )
        }

        // Long click listener to toggle message selection
        viewHolder.itemView.setOnLongClickListener {
            if (selectedMessages.isEmpty()) selectedMessages.add(messageId)
            Log.i(TAG, "Selected Message IDs: $selectedMessages")
            notifyItemChanged(viewHolder.adapterPosition) // Refresh view to update selection state
            true
        }

        // Short click listener to toggle message selection
        viewHolder.itemView.setOnClickListener {
            if (selectedMessages.isNotEmpty()) {
                toggleMessageSelection(messageId)
                notifyItemChanged(viewHolder.adapterPosition) // Refresh view to update selection state
            }
        }
        // Select message
        selectMessage(viewHolder, messageId)
    }

    private fun toggleMessageSelection(messageId: Int) {
        if (selectedMessages.contains(messageId)) {
            // If already selected, deselect the message
            selectedMessages.remove(messageId)
            Log.i(TAG, "Selected Message IDs: $selectedMessages")
        } else {
            // If not selected, select the message
            selectedMessages.add(messageId)
            Log.i(TAG, "Selected Message IDs: $selectedMessages")
        }
    }

    private fun selectMessage(
        viewHolder: ViewHolder,
        messageId: Int
    ) {
        val messageSelectionView =
            viewHolder.itemView.findViewById<View>(R.id.message_selection_view)
        messageSelectionView?.visibility =
            if (selectedMessages.contains(messageId)) View.VISIBLE else View.GONE
    }

    // Set Image Message And Timestamp
    private fun setImageData(viewHolder: ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val mediaMessage = (baseMessage as MediaMessage)
        Log.i(TAG, "File URL: ${mediaMessage.attachment.fileUrl}")
        if (viewHolder is LeftChatImageView) {
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

    // Set Text Message And Timestamp
    private fun setTextData(viewHolder: ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val textMessage = (baseMessage as TextMessage)

        when (viewHolder) {
            is LeftChatTextViewLong -> {
                viewHolder.binding.leftMessageTextView.text = textMessage.text
                viewHolder.binding.leftChatTimestampTxtView.text = getTimestamp(baseMessage.sentAt)
            }

            is LeftChatTextViewShort -> {
                viewHolder.binding.leftMessageTextView.text = textMessage.text
                viewHolder.binding.leftChatTimestampTxtView.text = getTimestamp(baseMessage.sentAt)
            }

            is RightChatTextViewLong -> {
                viewHolder.binding.rightChatMessageTxtView.text = textMessage.text
                viewHolder.binding.rightChatTimestampTxtView.text = getTimestamp(baseMessage.sentAt)
                setMessageStatusIcon(viewHolder, baseMessage)
            }

            else -> {
                viewHolder as RightChatTextViewShort
                viewHolder.binding.rightChatMessageTxtView.text = textMessage.text
                viewHolder.binding.rightChatTimestampTxtView.text = getTimestamp(baseMessage.sentAt)
                setMessageStatusIcon(viewHolder, baseMessage)
            }
        }
    }

    // Update the list on fetchPrevious
    fun updateList(baseMessageList: List<BaseMessage>) {
        setMessageList(baseMessageList)
    }

    // Get message timestamp
    private fun getTimestamp(timestamp: Long): String {
        val milliseconds = timestamp * 1000
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(milliseconds))
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    // Method to add the message that is received in real-time
    fun addMessage(baseMessage: BaseMessage) {
        this.messageList.add(baseMessage)
        notifyItemInserted(messageList.size - 1)
    }

    // Set Read/Delivered/Sent Icon
    private fun setMessageStatusIcon(
        viewHolder: ViewHolder,
        baseMessage: BaseMessage
    ) {
        if (viewHolder is RightChatTextViewLong) {
            if (baseMessage.readAt != 0L) {
                viewHolder.binding.messageStatusIcon.setImageResource(ic_delivered)
                viewHolder.binding.messageStatusIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, color.blue)
                )
            } else if (baseMessage.deliveredAt != 0L) {
                viewHolder.binding.messageStatusIcon.setImageResource(ic_delivered)
                viewHolder.binding.messageStatusIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, color.grey)
                )
            } else {
                viewHolder.binding.messageStatusIcon.setImageResource(ic_sent)
                viewHolder.binding.messageStatusIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, color.grey)
                )
            }
        } else {
            viewHolder as RightChatTextViewShort
            if (baseMessage.readAt != 0L) {
                viewHolder.binding.messageStatusIcon.setImageResource(ic_delivered)
                viewHolder.binding.messageStatusIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, color.blue)
                )
            } else if (baseMessage.deliveredAt != 0L) {
                viewHolder.binding.messageStatusIcon.setImageResource(ic_delivered)
                viewHolder.binding.messageStatusIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, color.grey)
                )
            } else {
                viewHolder.binding.messageStatusIcon.setImageResource(ic_sent)
                viewHolder.binding.messageStatusIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, color.grey)
                )
            }
        }
    }


    inner class LeftChatTextViewLong(val binding: LeftChatTextViewLongBinding) :
        ViewHolder(binding.root)

    inner class LeftChatTextViewShort(val binding: LeftChatTextViewShortBinding) :
        ViewHolder(binding.root)

    inner class RightChatTextViewLong(val binding: RightChatTextViewLongBinding) :
        ViewHolder(binding.root)

    inner class RightChatTextViewShort(val binding: RightChatTextViewShortBinding) :
        ViewHolder(binding.root)

    inner class LeftChatImageView(val binding: LeftChatImageViewBinding) :
        ViewHolder(binding.root)

    inner class RightChatImageView(val binding: RightChatImageViewBinding) :
        ViewHolder(binding.root)
}