package com.ali.whatsappplus.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.databinding.ChatItemBinding
import com.ali.whatsappplus.util.Constants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatAdapter(baseMessages: List<BaseMessage>) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    private var messageList: MutableList<BaseMessage> = ArrayList()
    private lateinit var binding: ChatItemBinding
    private lateinit var message: BaseMessage
    private var TAG = "ChatAdapter"

    init {
        setMessageList(baseMessages)
    }

    private fun setMessageList(messageList: List<BaseMessage>) {
        this.messageList.addAll(0, messageList)
        notifyItemRangeInserted(0, messageList.size)
    }

    inner class MyViewHolder(val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.MyViewHolder {
        binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: ChatAdapter.MyViewHolder, position: Int) {
        val baseMessage = messageList[position]
        var nextMessage: BaseMessage? = null
        var prevMessage: BaseMessage? = null
        var isNextMessage = false
        var isPreviousMessage = false
        var isPrevActionMessage = false
        val loggedInUser = CometChat.getLoggedInUser().uid
        val sender = baseMessage.sender.uid

        with(holder.binding){
            if (sender == loggedInUser){
                leftChatView.visibility = View.GONE
                rightChatView.visibility = View.VISIBLE
                if (baseMessage is TextMessage) {
                    rightChatMessageTxtView.text = baseMessage.text
                }
            } else {
                rightChatView.visibility = View.GONE
                leftChatView.visibility = View.VISIBLE
                if (baseMessage is TextMessage){
                    leftChatMessageTxtView.text = baseMessage.text
                }
            }
        }
    }

    fun updateList(baseMessageList: List<BaseMessage>) {
        setMessageList(baseMessageList)
    }

    private fun formatTime(milliseconds: Long): String {
        val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(milliseconds))
    }

    fun updateSentMessage(baseMessage: BaseMessage?) {
        for (i in messageList.indices.reversed()) {
            val muid = messageList[i].muid
            if (muid != null && muid == baseMessage?.muid) {
                messageList.removeAt(i)
                messageList.add(i, baseMessage)
                notifyItemChanged(i)
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun addMessage(textMessage: BaseMessage) {
        messageList.add(textMessage)
        notifyItemInserted(messageList.size - 1)
    }
}