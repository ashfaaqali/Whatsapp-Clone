package com.ali.whatsappplus.ui.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ali.whatsappplus.R
import com.ali.whatsappplus.R.color
import com.ali.whatsappplus.R.drawable.ic_delivered
import com.ali.whatsappplus.R.drawable.ic_sent
import com.ali.whatsappplus.databinding.ActionMessageViewBinding
import com.ali.whatsappplus.databinding.LeftChatImageViewBinding
import com.ali.whatsappplus.databinding.LeftChatTextViewLongBinding
import com.ali.whatsappplus.databinding.LeftChatTextViewShortBinding
import com.ali.whatsappplus.databinding.LeftDeletedMessageLayoutBinding
import com.ali.whatsappplus.databinding.RightChatImageViewBinding
import com.ali.whatsappplus.databinding.RightChatTextViewLongBinding
import com.ali.whatsappplus.databinding.RightChatTextViewShortBinding
import com.ali.whatsappplus.databinding.RightDeletedMessageLayoutBinding
import com.ali.whatsappplus.common.util.Constants
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(val context: Context, baseMessages: List<BaseMessage>) :
    RecyclerView.Adapter<ViewHolder>() {
    private var messageList: MutableList<BaseMessage> = ArrayList()
    private val selectedMessages: HashMap<Int, BaseMessage> = HashMap()
    private val selectedMessagesLiveData = MutableLiveData<List<Int>>()
    private val onImageClick: OnImageClick? = null
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
        if (baseMessage.deletedAt == 0L) {
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
            } else if (baseMessage is Action) {
                Constants.ACTION_MESSAGE_VIEW
            } else -1
        } else {
            return if (sender == loggedInUser) {
                Constants.RIGHT_DELETED_MESSAGE_VIEW
            } else {
                Constants.LEFT_DELETED_MESSAGE_VIEW
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Returning the appropriate view according to the ItemViewType
        return when (viewType) {
            Constants.RIGHT_DELETED_MESSAGE_VIEW -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val itemBinding: RightDeletedMessageLayoutBinding =
                    RightDeletedMessageLayoutBinding.inflate(layoutInflater, parent, false)
                itemBinding.root.tag = Constants.RIGHT_DELETED_MESSAGE_VIEW
                RightChatDeletedMessageView(itemBinding)
            }

            Constants.LEFT_DELETED_MESSAGE_VIEW -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val itemBinding: LeftDeletedMessageLayoutBinding =
                    LeftDeletedMessageLayoutBinding.inflate(layoutInflater, parent, false)
                itemBinding.root.tag = Constants.LEFT_DELETED_MESSAGE_VIEW
                LeftChatDeletedMessageView(itemBinding)
            }

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
                val actionItemBinding: ActionMessageViewBinding =
                    ActionMessageViewBinding.inflate(layoutInflater, parent, false)
                actionItemBinding.root.tag = Constants.ACTION_MESSAGE_VIEW
                ActionMessageView(actionItemBinding)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val messageId = baseMessage.id

        // Setting the data to the views according to the view type
        when (viewHolder.itemViewType) {
            Constants.LEFT_DELETED_MESSAGE_VIEW -> setDeletedMessageData(
                viewHolder as LeftChatDeletedMessageView,
                position
            )

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

            Constants.ACTION_MESSAGE_VIEW -> setActionData(
                viewHolder as ActionMessageView,
                position
            )
        }

        // Long click listener to toggle message selection
        viewHolder.itemView.setOnLongClickListener {
            if (selectedMessages.isEmpty()) {
                toggleMessageSelection(baseMessage)
                Log.d(TAG, "${viewHolder.itemViewType}")
            }
            notifyItemChanged(viewHolder.adapterPosition) // Refresh view to update selection state
            true
        }

        // Short click listener to toggle message selection
        viewHolder.itemView.setOnClickListener {
            if (selectedMessages.isNotEmpty()) {
                toggleMessageSelection(baseMessage)
                notifyItemChanged(viewHolder.adapterPosition) // Refresh view to update selection state
            } else {
                if (baseMessage is MediaMessage) {
                    if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                        val imageUrl = baseMessage.attachment.fileUrl
                        val imageList = ArrayList<String>()
                        imageList.add(imageUrl)
                        viewHolder.itemView.transitionName = "image_${baseMessage.id}"
                        viewHolder.itemView.setOnClickListener {
                            val urls = collectAllImageUrls()
                            val pos = urls.indexOf(baseMessage.attachment.fileUrl)

                            val intent = ImageViewer.createIntent(
                                context,
                                ArrayList(urls),
                                pos
                            )

                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                context as Activity,
                                viewHolder.itemView,
                                viewHolder.itemView.transitionName
                            )
                            // Open image viewer activity
                            options.toBundle()?.let { it1 ->
                                onImageClick?.onImageClick(
                                    ImageViewer.createIntent(context, imageList, 0),
                                    it1,
                                    imageList,
                                    viewHolder.itemView
                                )
                            }
                            // context.startActivity(intent, options.toBundle())
                        }

                        context.startActivity(
                            ImageViewer.createIntent(context, imageList, 0)
                        )
                    }
                }
            }
        }
        // Select message
        selectMessage(viewHolder, messageId)
    }

    private fun setActionData(viewHolder: ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val message = baseMessage as Action
        val actionMessage: TextView = viewHolder.itemView.findViewById(R.id.action_message)
        val actionCardView: CardView = viewHolder.itemView.findViewById(R.id.action_card_view)

        actionCardView.visibility = View.VISIBLE

        when (message.action) {
            CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED -> {
                val memberAdded = message.actionOn
                val currentUser = CometChat.getLoggedInUser()
                if (memberAdded is User) {
                    val text = currentUser.name + " added " + memberAdded.name
                    Log.wtf(TAG, "Action: $text")
                    actionMessage.text = text
                }
            }
        }
    }

    private fun setDeletedMessageData(viewHolder: ViewHolder, position: Int) {
        when (viewHolder) {
            is LeftChatDeletedMessageView -> {
                viewHolder.binding.leftDeletedMessage.visibility = View.VISIBLE
            }

            is RightChatDeletedMessageView -> {
                viewHolder.binding.rightDeletedMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleMessageSelection(baseMessage: BaseMessage) {
        if (selectedMessages.containsKey(baseMessage.id)) {
            // If already selected, deselect the message
            selectedMessages.remove(baseMessage.id)
        } else {
            // If not selected, select the message
            selectedMessages[baseMessage.id] = baseMessage
        }
        selectedMessagesLiveData.value = selectedMessages.keys.toList()
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

    fun getSelectedMessagesLiveData(): LiveData<List<Int>> {
        return selectedMessagesLiveData
    }

    // Set Image Message And Timestamp
    private fun setImageData(viewHolder: ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val mediaMessage = (baseMessage as MediaMessage)
        if (viewHolder is LeftChatImageView) {
            // val senderName: TextView = viewHolder.itemView.findViewById(R.id.sender_name)
            val senderProfilePic: ImageView = viewHolder.itemView.findViewById(R.id.sender_profile_pic)

            if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                // senderName.visibility = View.VISIBLE
                // enderName.text = baseMessage.sender.name
                senderProfilePic.visibility = View.VISIBLE

                if (baseMessage.sender.avatar != null) {
                    Glide.with(viewHolder.itemView)
                        .load(baseMessage.sender.avatar)
                        .into(senderProfilePic)

                    senderProfilePic.setBackgroundResource(R.drawable.circular_bg)
                    senderProfilePic.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, color.white))

                } else senderProfilePic.setImageResource(R.drawable.ic_user_profile)
            } else {
                // senderName.visibility = View.GONE
                senderProfilePic.visibility = View.GONE

                if (baseMessage.sender.avatar != null) {
                    Glide.with(viewHolder.itemView)
                        .load(baseMessage.sender.avatar)
                        .into(viewHolder.binding.senderProfilePic)

                    senderProfilePic.setBackgroundResource(R.drawable.circular_bg)
                    senderProfilePic.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, color.white))
                } else senderProfilePic.setImageResource(R.drawable.ic_user_profile)
            }

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

    private fun collectAllImageUrls(): List<String> {
        return messageList
            .filterIsInstance<MediaMessage>()
            .filter { it.type == CometChatConstants.MESSAGE_TYPE_IMAGE }
            .map { it.attachment.fileUrl }
    }

    // Set Text Message And Timestamp
    private fun setTextData(viewHolder: ViewHolder, position: Int) {
        val baseMessage = messageList[position]
        val textMessage = (baseMessage as TextMessage)

        when (viewHolder) {
            is LeftChatTextViewLong -> {
                val senderName: TextView = viewHolder.itemView.findViewById(R.id.sender_name)
                val senderProfilePic: ImageView = viewHolder.itemView.findViewById(R.id.sender_profile_pic)
                val leftMessageTextView: TextView = viewHolder.itemView.findViewById(R.id.left_message_text_view)
                val leftMessageTimestampTextView: TextView = viewHolder.itemView.findViewById(R.id.left_chat_timestamp_txt_view)

                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    senderName.visibility = View.VISIBLE
                    senderName.text = baseMessage.sender.name
                    senderProfilePic.visibility = View.VISIBLE

                    if (baseMessage.sender.avatar != null) {
                        Glide.with(viewHolder.itemView)
                            .load(baseMessage.sender.avatar)
                            .into(senderProfilePic)

                        senderProfilePic.setBackgroundResource(R.drawable.circular_bg)
                        senderProfilePic.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, color.white))

                    } else senderProfilePic.setImageResource(R.drawable.ic_user_profile)
                } else {
                    senderName.visibility = View.GONE
                    senderProfilePic.visibility = View.GONE

                    if (baseMessage.sender.avatar != null) {
                        Glide.with(viewHolder.itemView)
                            .load(baseMessage.sender.avatar)
                            .into(viewHolder.binding.senderProfilePic)

                        senderProfilePic.setBackgroundResource(R.drawable.circular_bg)
                        senderProfilePic.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, color.white))
                    } else senderProfilePic.setImageResource(R.drawable.ic_user_profile)
                }

                leftMessageTextView.text = textMessage.text
                leftMessageTimestampTextView.text = getTimestamp(baseMessage.sentAt)
            }

            is LeftChatTextViewShort -> {
                val senderName: TextView = viewHolder.itemView.findViewById(R.id.sender_name)
                val senderProfilePic: ImageView = viewHolder.itemView.findViewById(R.id.sender_profile_pic)
                val leftMessageTextView: TextView = viewHolder.itemView.findViewById(R.id.left_message_text_view)
                val leftMessageTimestampTextView: TextView = viewHolder.itemView.findViewById(R.id.left_chat_timestamp_txt_view)

                if (baseMessage.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
                    senderName.visibility = View.VISIBLE
                    senderName.text = baseMessage.sender.name
                    senderProfilePic.visibility = View.VISIBLE

                    if (baseMessage.sender.avatar != null) {
                        Glide.with(viewHolder.itemView)
                            .load(baseMessage.sender.avatar)
                            .into(senderProfilePic)

                        senderProfilePic.setBackgroundResource(R.drawable.circular_bg)
                        senderProfilePic.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, color.white))

                    } else senderProfilePic.setImageResource(R.drawable.ic_user_profile)
                } else {
                    senderName.visibility = View.GONE
                    senderProfilePic.visibility = View.GONE

                    if (baseMessage.sender.avatar != null) {
                        Glide.with(viewHolder.itemView)
                            .load(baseMessage.sender.avatar)
                            .into(viewHolder.binding.senderProfilePic)

                        senderProfilePic.setBackgroundResource(R.drawable.circular_bg)
                        senderProfilePic.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, color.white))
                    } else senderProfilePic.setImageResource(R.drawable.ic_user_profile)
                }

                leftMessageTextView.text = textMessage.text
                leftMessageTimestampTextView.text = getTimestamp(baseMessage.sentAt)
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

    fun removeMessage(baseMessage: BaseMessage) {
        if (messageList.contains(baseMessage)) {
            val index = messageList.indexOf(baseMessage)
            messageList.removeAt(index)
            notifyItemRemoved(index)
            // Add the deleted message at the same index
            addDeletedMessage(baseMessage, index)
            selectedMessages.remove(baseMessage.id)
            selectedMessagesLiveData.value = selectedMessages.keys.toList()
        }
    }

    private fun addDeletedMessage(baseMessage: BaseMessage, index: Int) {
        if (baseMessage.sender.uid == CometChat.getLoggedInUser().uid) {
            // Right side deleted message
            messageList.add(index, baseMessage)
        } else {
            // Left side deleted message
            messageList.add(index, baseMessage)
        }
        notifyItemInserted(index)
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

    inner class LeftChatDeletedMessageView(val binding: LeftDeletedMessageLayoutBinding) :
        ViewHolder(binding.root)

    inner class RightChatDeletedMessageView(val binding: RightDeletedMessageLayoutBinding) :
        ViewHolder(binding.root)

    inner class ActionMessageView(val binding: ActionMessageViewBinding) :
        ViewHolder(binding.root)
}

interface OnImageClick {
    fun onImageClick(intent: Intent, bundle: Bundle, imageUrl: ArrayList<String>, itemView: View)
}