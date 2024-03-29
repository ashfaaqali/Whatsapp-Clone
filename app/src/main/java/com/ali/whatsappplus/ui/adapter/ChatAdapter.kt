package com.ali.whatsappplus.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.data.model.Message
import com.ali.whatsappplus.databinding.ChatItemBinding
import com.ali.whatsappplus.util.Constants

class ChatAdapter(private var messageList: List<Message>) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.MyViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatAdapter.MyViewHolder, position: Int) {
        val message = messageList[position]

        with(holder.binding){
            if (message.source == Constants.SENT){
                leftChatView.visibility = View.GONE
                rightChatView.visibility = View.VISIBLE
                rightChatTextView.text = message.message
            } else {
                rightChatView.visibility = View.GONE
                leftChatView.visibility = View.VISIBLE
                leftChatTextView.text = message.message
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun setData(message: List<Message>){
        this.messageList = message
        notifyDataSetChanged()
    }
}