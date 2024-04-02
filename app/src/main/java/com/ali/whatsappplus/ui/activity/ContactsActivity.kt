package com.ali.whatsappplus.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.databinding.ActivityContactsBinding
import com.ali.whatsappplus.ui.adapter.AllContactsAdapter
import com.ali.whatsappplus.viewmodel.AllContactsViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.core.UsersRequest.UsersRequestBuilder
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: AllContactsAdapter
    private lateinit var viewModel: AllContactsViewModel
    private lateinit var usersRequest: UsersRequest
    private val TAG = "ContactsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AllContactsAdapter(emptyList())

        usersRequest = UsersRequestBuilder()
            .setLimit(30)
            .build()

        usersRequest.fetchNext(object : CometChat.CallbackListener<List<User>>(){
            override fun onSuccess(userList: List<User>?) {
                if (userList != null){
                    adapter.setData(userList)
                    Log.i(TAG, "User List: $userList")
                }
            }

            override fun onError(p0: CometChatException?) {
                Log.i(TAG, "$p0")
            }
        })

        adapter.listener = object : AllContactsAdapter.OnContactItemClickListener{
            override fun onContactItemClicked(user: User) {
                navigateToChatActivity(user)
            }
        }

        viewModel = ViewModelProvider(this)[AllContactsViewModel::class.java]
        viewModel.loadRecentContactsFromAssets(this, "allContacts.json")
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun navigateToChatActivity(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("name", user.name)
        intent.putExtra("avatar", user.avatar)
        intent.putExtra("receiverId", user.uid)
        startActivity(intent)
    }
}