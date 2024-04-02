package com.ali.whatsappplus.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ChatItemBinding
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.TextMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatAdapter(private var messageList: List<TextMessage>) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    private lateinit var binding: ChatItemBinding
    private lateinit var message: TextMessage

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
        message = messageList[position]
        val loggedInUser = CometChat.getLoggedInUser().uid
        val sender = message.sender.uid

        with(holder.binding){
            if (sender == loggedInUser){
                leftChatView.visibility = View.GONE
                rightChatView.visibility = View.VISIBLE
                rightChatMessageTxtView.text = message.text
                Log.i("ChatAdapter Right", "Sent Message: [${message}]")
            } else {
                rightChatView.visibility = View.GONE
                leftChatView.visibility = View.VISIBLE
                leftChatMessageTxtView.text = message.text
                Log.i("ChatAdapter Left", "Received Message: $message")
            }
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(milliseconds))
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun updateMessageStatus(sentAt: Long) {
        binding.messageStatusIcon.setImageResource(R.drawable.ic_sent)
        binding.rightChatTimestampTxtView.visibility = View.VISIBLE
        binding.rightChatTimestampTxtView.text = formatTime(sentAt)
    }

    fun markMessageAsRead() {

    }
}