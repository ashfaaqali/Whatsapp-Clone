package com.ali.whatsappplus.ui.fragment
// Ongoing Chats (Launch Screen)

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.databinding.FragmentRecentChatsBinding
import com.ali.whatsappplus.ui.activity.ChatActivity
import com.ali.whatsappplus.ui.adapter.RecentChatsAdapter
import com.ali.whatsappplus.viewmodel.RecentChatsViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.ConversationsRequest.ConversationsRequestBuilder
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation

class RecentChatsFragment : Fragment() {
    private lateinit var binding: FragmentRecentChatsBinding
    private lateinit var viewModel: RecentChatsViewModel
    private lateinit var adapter: RecentChatsAdapter

    private lateinit var conversationsRequest: ConversationsRequest

    private val TAG = "RecentChatsFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRecentChatsBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this)[RecentChatsViewModel::class.java]

        adapter = RecentChatsAdapter(emptyList())

        adapter.listener = object : RecentChatsAdapter.OnChatItemClickListener {
            override fun onChatItemClicked(
                username: String,
                uid: String,
                avatar: String,
                receiverType: String
            ) {
                navigateToChatActivity(username, uid, avatar)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchConversation()

        binding.recentChatsRecyclerView.adapter = adapter
        binding.recentChatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchConversation() {
        conversationsRequest = ConversationsRequestBuilder()
            .setLimit(20)
            .build()

        conversationsRequest.fetchNext(object : CometChat.CallbackListener<List<Conversation>>() {
            override fun onSuccess(conversationList: List<Conversation>?) {
                //Handle List of Conversations
                if (conversationList != null){
                    Log.i(TAG, " Conversation List: $conversationList")
                    adapter.setData(conversationList)
                }
            }

            override fun onError(exception: CometChatException?) {
                //Handle Failure
                Log.i(TAG, "Fetch Conversation List Failed: $exception")
            }
        })
    }

    private fun navigateToChatActivity(name: String, uid: String, avatar: String) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("receiverId", uid)
        intent.putExtra("avatar", avatar)
        startActivity(intent)
    }

}