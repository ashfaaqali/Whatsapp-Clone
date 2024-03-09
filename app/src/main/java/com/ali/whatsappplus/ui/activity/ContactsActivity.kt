package com.ali.whatsappplus.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.R
import com.ali.whatsappplus.data.model.AllContacts
import com.ali.whatsappplus.databinding.ActivityContactsBinding
import com.ali.whatsappplus.ui.adapter.AllContactsAdapter
import com.ali.whatsappplus.viewmodel.AllContactsViewModel

class ContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: AllContactsAdapter
    private lateinit var viewModel: AllContactsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AllContactsAdapter(emptyList())
        adapter.listener = object : AllContactsAdapter.OnContactItemClickListener{
            override fun onContactItemClicked(contacts: AllContacts) {
                navigateToChatActivity(contacts)
            }

        }

        viewModel = ViewModelProvider(this)[AllContactsViewModel::class.java]
        viewModel.loadRecentContactsFromAssets(this, "allContacts.json")
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.allContacts.observe(this, Observer {
            adapter.setData(it)
        })
    }

    private fun navigateToChatActivity(chat: AllContacts) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("name", chat.name)
        startActivity(intent)
    }
}