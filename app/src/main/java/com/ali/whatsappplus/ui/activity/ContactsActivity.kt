package com.ali.whatsappplus.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.databinding.ActivityContactsBinding
import com.ali.whatsappplus.ui.adapter.AllContactsAdapter
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.core.UsersRequest.UsersRequestBuilder
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: AllContactsAdapter
    private lateinit var usersRequest: UsersRequest
    private val TAG = "ContactsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerViewSetup()

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

        // Click listener for contacts
        adapter.listener = object : AllContactsAdapter.OnContactItemClickListener{
            override fun onContactItemClicked(user: User) {
                navigateToChatActivity(user)
            }
        }
    }

    private fun recyclerViewSetup() {
        adapter = AllContactsAdapter(emptyList())
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
        startActivity(intent)
    }
}