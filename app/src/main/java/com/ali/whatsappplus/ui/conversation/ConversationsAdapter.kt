package com.ali.whatsappplus.ui.conversation

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.RecentChatItemBinding
import com.bumptech.glide.Glide
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.*
import java.text.SimpleDateFormat
import java.util.*

class ConversationsAdapter(
    private val context: Context,
    private var conversationList: List<Conversation>
) : RecyclerView.Adapter<ConversationsAdapter.MyViewHolder>() {

    var listener: OnChatItemClickListener? = null
    private val currentUser = CometChat.getLoggedInUser()
    private val selectedConversation: MutableMap<Int, Conversation> = mutableMapOf()
    private val selectedConversationLiveData = MutableLiveData<List<Int>>()

    // ViewHolder class to hold the view references for each item in the RecyclerView
    inner class MyViewHolder(val binding: RecentChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Binds the data to the views for each conversation item
        fun bind(conversation: Conversation) {
            val entity: AppEntity = conversation.conversationWith
            val message: BaseMessage? = conversation.lastMessage

            // Set the conversation name
            binding.conversationName.text = when (entity) {
                is User -> entity.name
                is Group -> entity.name
                else -> ""
            }

            // Load the profile picture
            Glide.with(binding.root)
                .load(
                    when (entity) {
                        is User -> entity.avatar
                        is Group -> entity.icon
                        else -> null
                    } ?: getDefaultProfilePic(entity) // If URL is null, use default profile picture
                )
                .into(binding.profilePic)

            // Configure profile picture for groups
            if (entity is Group) {
                configureGroupProfilePic(binding.profilePic)
            }

            // Set the last message text and timestamp if available
            message?.let {
                binding.lastMessage.text = getMessageText(it) // Get the last message
                binding.lastMessageTime.text = getTimestamp(it.sentAt) // Get last message timestamp
                binding.unreadMessageCount.text =
                    getUnreadMessageCount(conversation.unreadMessageCount) // Get unread message count
                binding.unreadMessageCount.visibility =
                    if (conversation.unreadMessageCount > 0) View.VISIBLE else View.GONE
            }

            // Handle long click to toggle conversation selection
            binding.root.setOnLongClickListener {
                if (selectedConversation.isEmpty()) toggleConversationSelection(conversation)
                notifyItemChanged(adapterPosition)
                true
            }

            // Handle click to toggle conversation selection or open conversation
            binding.root.setOnClickListener {
                if (selectedConversation.isNotEmpty()) {
                    toggleConversationSelection(conversation)
                    notifyItemChanged(adapterPosition)
                } else {
                    openConversation(entity, conversation)
                }
            }
            // Highlight selected conversation
            selectConversation(binding, message?.id)
        }

        // Returns the default profile picture resource based on the entity type
        private fun getDefaultProfilePic(entity: AppEntity): Int {
            return if (entity is User) R.drawable.ic_user_profile else R.drawable.ic_group_profile
        }

        // Configures the profile picture view for group entities
        private fun configureGroupProfilePic(profilePic: ImageView) {
            profilePic.setPadding(20, 20, 20, 20)
            profilePic.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            profilePic.background = ContextCompat.getDrawable(context, R.drawable.circular_bg)
            profilePic.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_shade))
        }

        // Returns the appropriate message text based on the message type
        private fun getMessageText(message: BaseMessage): String {
            return when (message) {
                is TextMessage -> {
                    if (message.deletedAt != 0L) {
                        context.getString(R.string.this_messages_was_deleted)
                    } else {
                        message.text
                    }
                }

                is Action -> message.message
                is MediaMessage -> {
                    if (message.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                        if (message.sender.uid == currentUser.uid) {
                            context.getString(R.string.you_sent_image, "You")
                        } else {
                            context.getString(R.string.you_sent_image, message.sender.name)
                        }
                    } else ""
                }

                else -> ""
            }
        }

        // Returns the formatted timestamp
        private fun getTimestamp(timestamp: Long): String {
            val milliseconds = timestamp * 1000
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return sdf.format(Date(milliseconds))
        }

        // Returns the unread message count as a string
        private fun getUnreadMessageCount(count: Int): String {
            return if (count in 1..10) count.toString() else "10+"
        }

        // Highlights the selected conversation
        private fun selectConversation(binding: RecentChatItemBinding, messageId: Int?) {
            messageId?.let {
                binding.messageSelectionView.visibility =
                    if (selectedConversation.containsKey(it)) View.VISIBLE else View.GONE
            }
        }

        // Toggles the selection state of a conversation
        private fun toggleConversationSelection(conversation: Conversation) {
            if (selectedConversation.containsKey(conversation.lastMessage.id)) {
                selectedConversation.remove(conversation.lastMessage.id)
            } else {
                selectedConversation[conversation.lastMessage.id] = conversation
            }
            selectedConversationLiveData.postValue(selectedConversation.keys.toList())
        }

        // Opens the conversation based on the entity type
        private fun openConversation(entity: AppEntity, conversation: Conversation) {
            if (entity is User) {
                listener?.onChatItemClicked(
                    entity.name,
                    entity.uid,
                    entity.avatar,
                    conversation.lastMessage.receiverType
                )
            } else if (entity is Group) {
                listener?.onChatItemClicked(
                    entity.name,
                    entity.guid,
                    entity.icon,
                    conversation.lastMessage.receiverType
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RecentChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(conversationList[position])
    }

    override fun getItemCount(): Int = conversationList.size

    // Updates the conversation list and notifies the adapter
    fun setData(conversationList: List<Conversation>) {
        this.conversationList = conversationList
        notifyDataSetChanged()
    }

    // Returns a LiveData object representing the selected conversation IDs
    fun getSelectedConversationLiveData(): LiveData<List<Int>> = selectedConversationLiveData

    // Interface for handling chat item clicks
    interface OnChatItemClickListener {
        fun onChatItemClicked(
            username: String,
            uid: String,
            avatar: String? = null,
            receiverType: String
        )
    }
}
