package com.ali.whatsappplus.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.RecentChatItemBinding
import com.bumptech.glide.Glide
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
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
    private var TAG = "RecentChatsAdapter"
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
        Log.i("RecentChatsAdapter", "Conversation: $conversation")

        val entity = conversation.conversationWith
        val message = conversation.lastMessage

        with(holder.binding) {

            // Check if the conversation is with User or Group
            if (entity is User) {
                conversationName.text = entity.name
                Glide.with(holder.itemView)
                    .load(entity.avatar)
                    .into(profilePic)
            } else if (entity is Group) {
                conversationName.text = entity.name
                Glide.with(holder.itemView)
                    .load(entity.icon)
                    .into(profilePic)
            }

            // Check message type
            if (message is TextMessage) {
                if (message.deletedAt == 0L) {
                    lastMessage.text = message.text
                } else {
                    lastMessage.text =
                        ContextCompat.getString(context, R.string.this_messages_was_deleted)
                }
            }

            if (message.sender == currentUser) {
                lastMessageTime.text = formatTime(message.sentAt)
            } else {
                lastMessageTime.text = formatTime(message.deliveredToMeAt)
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

    private fun formatTime(milliseconds: Long): String {
        val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
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
        fun onChatItemClicked(username: String, uid: String, avatar: String, receiverType: String)
    }
}


