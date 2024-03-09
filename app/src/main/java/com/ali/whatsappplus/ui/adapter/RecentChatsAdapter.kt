package com.ali.whatsappplus.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.data.model.RecentChats
import com.ali.whatsappplus.databinding.RecentChatItemBinding

class RecentChatsAdapter(private var chatList: List<RecentChats>) :
    RecyclerView.Adapter<RecentChatsAdapter.MyViewHolder>() {

    var listener: OnChatItemClickListener? = null

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
        val chat = chatList[position]

        with(holder.binding){
            contactName.text = chat.contactName
            lastMessage.text = chat.lastMessage
            lastMessageTime.text = chat.timestamp
        }

        holder.itemView.setOnClickListener{
            listener?.onChatItemClicked(chat)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun setData(recentChats: List<RecentChats>){
        this.chatList = recentChats
        notifyDataSetChanged()
    }

    interface OnChatItemClickListener {
        fun onChatItemClicked(chat: RecentChats)
    }
}


