package com.ali.whatsappplus.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
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
    RecyclerView.Adapter<RecentChatsAdapter.MyViewHolder>() {

    var listener: OnChatItemClickListener? = null
    private val tag = "RecentChatsAdapter"
    private var currentUser = CometChat.getLoggedInUser()

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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val conversation = conversationList[position]
        Log.i(tag, "Conversation: $conversation")

        val entity: AppEntity = conversation.conversationWith
        val message: BaseMessage = conversation.lastMessage

        with(holder.binding) {
            // Check if the conversation is with User or Group
            if (entity is User) { // If entity is User
                conversationName.text = entity.name
                if (entity.avatar != null) {
                    Glide.with(holder.itemView)
                        .load(entity.avatar)
                        .into(profilePic)
                } else {
                    profilePic.setImageResource(R.drawable.ic_user_profile)
                }

            } else if (entity is Group) { // If entity is Group
                conversationName.text = entity.name
                if (entity.icon != null) {
                    Glide.with(holder.itemView)
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

            // Check message type
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
                    if (message.sender.uid == CometChat.getLoggedInUser().uid){
                        val text = "You " + ContextCompat.getString(context, R.string.you_sent_image)
                        lastMessage.text = text
                    } else {
                        val text = message.sender.name + " " + ContextCompat.getString(context, R.string.you_sent_image)
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
        }

        // Click listener for conversations
        holder.itemView.setOnClickListener {
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

    // Get last message timestamp
    private fun getTimestamp(timestamp: Long): String {
        val milliseconds = timestamp * 1000
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(milliseconds))
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


