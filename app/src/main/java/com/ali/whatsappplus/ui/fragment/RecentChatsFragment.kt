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
import com.cometchat.chat.helpers.CometChatHelper
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage

class RecentChatsFragment : Fragment() {
    private lateinit var binding: FragmentRecentChatsBinding
    private lateinit var viewModel: RecentChatsViewModel
    private lateinit var adapter: RecentChatsAdapter
    private lateinit var sortedConversationList: List<Conversation>
    private lateinit var conversationsRequest: ConversationsRequest

    private val TAG = "RecentChatsFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentChatsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[RecentChatsViewModel::class.java]
        adapter = RecentChatsAdapter(emptyList())
        // Handling Item Click
        adapter.listener = object : RecentChatsAdapter.OnChatItemClickListener {
            override fun onChatItemClicked(
                username: String,
                uid: String,
                avatar: String,
                receiverType: String
            ) {
                navigateToChatActivity(username, uid, avatar, receiverType)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Real-Time Message Listener
        messageListener()
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
                    stopShimmer()
                    Log.i(TAG, " Conversation List: $conversationList")
                    sortedConversationList = conversationList.sortedWith(compareByDescending { it.lastMessage.sentAt })
                    adapter.setData(sortedConversationList)
                }
            }

            override fun onError(exception: CometChatException?) {
                //Handle Failure
                Log.i(TAG, "Fetch Conversation List Failed: $exception")
            }
        })
    }

    private fun stopShimmer() {
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
    }

    private fun messageListener() {
        val listenerID = TAG
        CometChat.addMessageListener(listenerID, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(textMessage: TextMessage) {
                Log.d(TAG, "Realtime text message received successfully: $textMessage")
                // Get the conversation associated with the received message
                val conversation = CometChatHelper.getConversationFromMessage(textMessage)
                // Update the conversation list with the new/updated conversation
                val updatedList = sortedConversationList.toMutableList()
                val existingIndex = updatedList.indexOfFirst { it.conversationId == conversation.conversationId }
                if (existingIndex != -1) {
                    updatedList[existingIndex] = conversation // Update existing conversation
                } else {
                    updatedList.add(conversation) // Add new conversation
                }
                // Sort the conversation list based on the latest message time
                sortedConversationList = updatedList.sortedByDescending { it.lastMessage.sentAt }
                // Update the adapter with the sorted conversation list
                adapter.setData(sortedConversationList)
            }

            override fun onMediaMessageReceived(mediaMessage: MediaMessage) {
                Log.d(TAG, "Media message received successfully: $mediaMessage")
            }

            override fun onCustomMessageReceived(customMessage: CustomMessage) {
                Log.d(TAG, "Custom message received successfully: $customMessage")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Fetch Conversations
        fetchConversation()
        binding.shimmer.startShimmer()
    }

    private fun navigateToChatActivity(
        name: String,
        uid: String,
        avatar: String,
        receiverType: String
    ) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("receiverId", uid)
        intent.putExtra("avatar", avatar)
        intent.putExtra("receiverType", receiverType)
        startActivity(intent)
    }

}