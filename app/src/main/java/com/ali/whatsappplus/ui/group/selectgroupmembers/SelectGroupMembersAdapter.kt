package com.ali.whatsappplus.ui.group.selectgroupmembers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.AllContactsItemBinding
import com.bumptech.glide.Glide
import com.cometchat.chat.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SelectGroupMembersAdapter(private var userList: List<User>) :
    RecyclerView.Adapter<ViewHolder>() {
    private lateinit var userName: TextView
    private lateinit var userStatus: TextView
    private lateinit var userAvatar: ImageView
    private lateinit var checkIcon: ImageView
    private val selectedMembers: HashMap<String, User> = HashMap()
    private val selectedMembersLiveData = MutableLiveData<List<String>>()
    var listener: OnContactItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            AllContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val user = userList[position]

        userName = viewHolder.itemView.findViewById(R.id.contact_name)
        userStatus = viewHolder.itemView.findViewById(R.id.contact_status)
        userAvatar = viewHolder.itemView.findViewById(R.id.profile_pic)
        checkIcon = viewHolder.itemView.findViewById(R.id.check)

        setDataToViews(user, viewHolder)

        viewHolder.itemView.setOnClickListener {
            selectMembers(user, viewHolder)
        }
    }

    private fun manageFabVisibility(nextFab: FloatingActionButton) {
        if (selectedMembers.isNotEmpty()) nextFab.visibility =
            View.VISIBLE else nextFab.visibility = View.GONE
    }

    private fun setDataToViews(user: User, viewHolder: ViewHolder) {
        userName.text = user.name
        userStatus.text = user.status
        Glide.with(viewHolder.itemView)
            .load(user.avatar)
            .into(userAvatar)
    }

    private fun selectMembers(user: User, viewHolder: ViewHolder) {
        if (selectedMembers.containsKey(user.uid)) {
            // If already selected, deselect the message
            selectedMembers.remove(user.uid)
            viewHolder.itemView.findViewById<ImageView>(R.id.check).visibility = View.GONE
        } else {
            // If not selected, select the message
            selectedMembers[user.uid] = user
            viewHolder.itemView.findViewById<ImageView>(R.id.check).visibility = View.VISIBLE
        }
        selectedMembersLiveData.value = selectedMembers.keys.toList()
    }

    fun selectedMembersLiveData(): MutableLiveData<List<String>> {
        return selectedMembersLiveData
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setData(userList: List<User>) {
        this.userList = userList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val binding: AllContactsItemBinding) :
        ViewHolder(binding.root)

    interface OnContactItemClickListener {
        fun onContactItemClicked(user: User)
    }

}