package com.ali.whatsappplus.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.databinding.ActivityContactsBinding
import com.ali.whatsappplus.ui.group.GroupActivity
import com.ali.whatsappplus.ui.chat.ChatActivity
import com.ali.whatsappplus.common.util.Constants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.core.UsersRequest.UsersRequestBuilder
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: ContactsAdapter
    private lateinit var usersRequest: UsersRequest
    private val TAG = "ContactsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerViewSetup()
        // Click listener for contacts
        adapter.listener = object : ContactsAdapter.OnContactItemClickListener {
            override fun onContactItemClicked(user: User) {
                navigateToChatActivity(user)
            }
        }
        fetchUsers()

        binding.newGroup.setOnClickListener {
            navigateToSelectGroupMembersActivity()
        }

        binding.back.setOnClickListener {
            finish()
        }
    }

    private fun navigateToSelectGroupMembersActivity() {
        val intent = Intent(this, GroupActivity::class.java)
        intent.putExtra(Constants.FRAGMENT_TO_LOAD, Constants.FRAGMENT_SELECT_GROUP_MEMBERS)
        startActivity(intent)
    }

    private fun fetchUsers() {
        // Request For Users
        usersRequest = UsersRequestBuilder()
            .setLimit(30)
            .build()

        usersRequest.fetchNext(object : CometChat.CallbackListener<List<User>>(){
            override fun onSuccess(userList: List<User>?) {
                if (userList != null){
                    stopShimmer()
                    adapter.setData(userList)
                    Log.i(TAG, "User List: $userList")
                }
            }

            override fun onError(p0: CometChatException?) {
                Log.i(TAG, "$p0")
            }
        })
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun recyclerViewSetup() {
        adapter = ContactsAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun stopShimmer() {
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        binding.shimmer.startShimmer()
    }

    private fun navigateToChatActivity(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("name", user.name)
        intent.putExtra("avatar", user.avatar)
        intent.putExtra("receiverId", user.uid)
        intent.putExtra("receiverType", "user")
        startActivity(intent)
    }
}