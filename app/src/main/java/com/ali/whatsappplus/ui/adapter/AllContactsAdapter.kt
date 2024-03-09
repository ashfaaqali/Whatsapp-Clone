package com.ali.whatsappplus.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ali.whatsappplus.data.model.AllContacts
import com.ali.whatsappplus.databinding.AllContactsItemBinding

class AllContactsAdapter(private var contactList: List<AllContacts>) :
    RecyclerView.Adapter<AllContactsAdapter.MyViewHolder>() {

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
        val contact = contactList[position]

        with(holder.binding) {
            contactName.text = contact.name
            contactStatus.text = contact.status
        }

        holder.itemView.setOnClickListener{
            listener?.onContactItemClicked(contact)
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    fun setData(allContacts: List<AllContacts>) {
        this.contactList = allContacts
        notifyDataSetChanged()
    }

    interface OnContactItemClickListener {
        fun onContactItemClicked(contacts: AllContacts)
    }

}