package com.ali.whatsappplus.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.CallLogItemBinding
import com.bumptech.glide.Glide
import com.cometchat.calls.model.CallGroup
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser

class CallLogAdapter(val context: Context, private var callLogList: List<CallLog>) :
    RecyclerView.Adapter<ViewHolder>() {
    private val tag = "CallLogAdapter"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CallLogAdapter.MyViewHolder {
        val binding =
            CallLogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return callLogList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val callLog = callLogList[position]
        val entity = callLog.initiator

        // Item Views
        val profilePicture: ImageView = viewHolder.itemView.findViewById(R.id.profile_pic)
        val callerName: TextView = viewHolder.itemView.findViewById(R.id.caller_name)
        val callIcon: ImageView = viewHolder.itemView.findViewById(R.id.call_icon)
        val callDate: TextView = viewHolder.itemView.findViewById(R.id.call_date)
        val callTime: TextView = viewHolder.itemView.findViewById(R.id.call_time)
        val callBtn: ImageView = viewHolder.itemView.findViewById(R.id.call_btn)

        if (entity is CallUser) {
            callerName.text = entity.name
            if (entity.avatar != null) {
                Glide.with(viewHolder.itemView)
                    .load(entity.avatar)
                    .into(profilePicture)
            } else {
                profilePicture.setImageResource(R.drawable.ic_user_profile)
            }
        }

        if (entity is CallGroup) {
            callerName.text = entity.name
            if (entity.icon != null) {
                Glide.with(viewHolder.itemView)
                    .load(entity.icon)
                    .into(profilePicture)
            } else {
                profilePicture.setImageResource(R.drawable.ic_group_profile)
                profilePicture.setPadding(20, 20, 20, 20)
                profilePicture.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                profilePicture.background =
                    ContextCompat.getDrawable(context, R.drawable.circular_bg)
                profilePicture.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_shade))
            }
        }

    }

    fun setData(callLogList: List<CallLog>) {
        this.callLogList = callLogList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val binding: CallLogItemBinding) :
        ViewHolder(binding.root)

}