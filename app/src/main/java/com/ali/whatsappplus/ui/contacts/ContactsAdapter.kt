package com.ali.whatsappplus.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.AllContactsItemBinding
import com.bumptech.glide.Glide
import com.cometchat.chat.models.User

class ContactsAdapter(private var userList: List<User>) :
    RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {

    var listener: OnContactItemClickListener? = null

    inner class MyViewHolder(val binding: AllContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding =
            AllContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = userList[position]

        with(holder.binding) {
            contactName.text = user.name
            contactStatus.text = user.status

            if (user.avatar!=null){
                Glide.with(holder.itemView)
                    .load(user.avatar)
                    .into(profilePic)
            } else {
                profilePic.setImageResource(R.drawable.ic_user_profile)
            }
        }

        holder.itemView.setOnClickListener{
            listener?.onContactItemClicked(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setData(userList: List<User>) {
        this.userList = userList
        notifyDataSetChanged()
    }

    interface OnContactItemClickListener {
        fun onContactItemClicked(user: User)
    }

}