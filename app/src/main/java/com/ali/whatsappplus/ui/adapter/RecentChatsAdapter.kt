package com.ali.whatsappplus.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.RecentChatItemBinding
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.AppEntity
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RecentChatsAdapter(
    val context: Context,
    private var conversationList: List<Conversation>
) :
    RecyclerView.Adapter<ViewHolder>() {

    var listener: OnChatItemClickListener? = null
    private val tag = "RecentChatsAdapter"
    private var currentUser = CometChat.getLoggedInUser()
    private val selectedConversation: HashMap<Int, BaseMessage> = HashMap()
    private val selectedConversationLiveData = MutableLiveData<List<Int>>()

    inner class MyViewHolder(val binding: RecentChatItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding =
            RecentChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val conversation = conversationList[position]

        val entity: AppEntity = conversation.conversationWith
        val message: BaseMessage = conversation.lastMessage

        // Views
        val conversationName: TextView = viewHolder.itemView.findViewById(R.id.conversation_name)
        val profilePic: ImageView = viewHolder.itemView.findViewById(R.id.profile_pic)
        val lastMessage: TextView = viewHolder.itemView.findViewById(R.id.last_message)
        val lastMessageTime: TextView = viewHolder.itemView.findViewById(R.id.last_message_time)
        val unreadMessageCount: TextView = viewHolder.itemView.findViewById(R.id.unread_message_count)

        // Set User Avatar
        // If entity is User
        if (entity is User) {
            conversationName.text = entity.name
            if (entity.avatar != null) {
                Glide.with(viewHolder.itemView)
                    .load(entity.avatar)
                    .into(profilePic)
            } else {
                profilePic.setImageResource(R.drawable.ic_user_profile)
            }
        } else if (entity is Group) { // If entity is Group
            conversationName.text = entity.name
            if (entity.icon != null) {
                Glide.with(viewHolder.itemView)
                    .load(entity.icon)
                    .into(profilePic)
            } else {
                profilePic.setImageResource(R.drawable.ic_group_profile)
                profilePic.setPadding(20, 20, 20, 20)
                profilePic.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                profilePic.background =
                    ContextCompat.getDrawable(context, R.drawable.circular_bg)
                profilePic.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_shade))
            }
        }

        // Set Last Message Text
        if (message is TextMessage) {
            if (message.deletedAt != 0L) {
                lastMessage.text =
                    ContextCompat.getString(context, R.string.this_messages_was_deleted)
            } else {
                lastMessage.text = message.text
            }
        } else if (message is Action) {
            lastMessage.text = message.message
        } else if (message is MediaMessage) {
            if (message.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                if (message.sender.uid == CometChat.getLoggedInUser().uid) {
                    val text =
                        "You " + ContextCompat.getString(context, R.string.you_sent_image)
                    lastMessage.text = text
                } else {
                    val text = message.sender.name + " " + ContextCompat.getString(
                        context,
                        R.string.you_sent_image
                    )
                    lastMessage.text = text
                }
            }
        }

        // Set Last Message Timestamp
        if (message.sender == currentUser) {
            lastMessageTime.text = getTimestamp(message.sentAt)
        } else {
            lastMessageTime.text = getTimestamp(message.deliveredToMeAt)
        }

        // Set unread messages count
        if (conversation.unreadMessageCount > 0) {
            if (conversation.unreadMessageCount in 1..10) {
                unreadMessageCount.visibility = View.VISIBLE
                unreadMessageCount.text = conversation.unreadMessageCount.toString()
            } else {
                unreadMessageCount.visibility = View.VISIBLE
                unreadMessageCount.text = "10+"
            }
        } else {
            unreadMessageCount.visibility = View.GONE
        }

        // Long Click Listener To Toggle Message Selection
        viewHolder.itemView.setOnLongClickListener {
            if (selectedConversation.isEmpty()) {
                toggleConversationSelection(message)
            }
            notifyItemChanged(viewHolder.adapterPosition) // Refresh View To Update Selection State
            true
        }

        // Short Click listener To Toggle Message Selection And Conversation Opening
        viewHolder.itemView.setOnClickListener {
            if (selectedConversation.isNotEmpty()) {
                toggleConversationSelection(message)
                notifyItemChanged(viewHolder.adapterPosition) // Refresh View To Update Selection State
            } else {
                if (entity is User) {
                    listener?.onChatItemClicked(
                        entity.name,
                        entity.uid,
                        entity.avatar,
                        conversation.lastMessage.receiverType
                    )
                }
                if (entity is Group) {
                    listener?.onChatItemClicked(
                        entity.name,
                        entity.guid,
                        entity.icon,
                        conversation.lastMessage.receiverType
                    )
                }
            }
        }

        // Select message
        selectConversation(viewHolder, message.id)
    }

    // Get last message timestamp
    private fun getTimestamp(timestamp: Long): String {
        val milliseconds = timestamp * 1000
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(milliseconds))
    }

    private fun selectConversation(
        viewHolder: RecyclerView.ViewHolder,
        messageId: Int
    ) {
        val messageSelectionView =
            viewHolder.itemView.findViewById<View>(R.id.message_selection_view)
        messageSelectionView?.visibility =
            if (selectedConversation.contains(messageId)) View.VISIBLE else View.GONE
    }

    fun getSelectedConversationLiveData(): LiveData<List<Int>> {
        return selectedConversationLiveData
    }

    private fun toggleConversationSelection(baseMessage: BaseMessage) {
        if (selectedConversation.containsKey(baseMessage.id)) {
            // If already selected, deselect the message
            selectedConversation.remove(baseMessage.id)
        } else {
            // If not selected, select the message
            selectedConversation[baseMessage.id] = baseMessage
        }
        selectedConversationLiveData.postValue(selectedConversation.keys.toList())
    }


    override fun getItemCount(): Int {
        return conversationList.size
    }

    fun setData(conversationList: List<Conversation>) {
        this.conversationList = conversationList
        notifyDataSetChanged()
    }

    interface OnChatItemClickListener {
        fun onChatItemClicked(
            username: String,
            uid: String,
            avatar: String? = null,
            receiverType: String
        )
    }
}


