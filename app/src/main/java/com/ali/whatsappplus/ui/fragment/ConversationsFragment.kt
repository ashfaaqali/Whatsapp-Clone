package com.ali.whatsappplus.ui.fragment
// Ongoing Chats (Launch Screen)

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.FragmentRecentChatsBinding
import com.ali.whatsappplus.ui.activity.ChatActivity
import com.ali.whatsappplus.ui.activity.MainActivity
import com.ali.whatsappplus.ui.adapter.RecentChatsAdapter
import com.ali.whatsappplus.util.Constants
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

class ConversationsFragment : Fragment() {

    private lateinit var binding: FragmentRecentChatsBinding
    private lateinit var viewModel: RecentChatsViewModel
    private lateinit var adapter: RecentChatsAdapter
    private var conversationList: MutableList<Conversation> = mutableListOf()
    private lateinit var conversationsRequest: ConversationsRequest
    private val tag = "ConversationsFragment"
    private var selectedConversations: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentChatsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[RecentChatsViewModel::class.java]
        recyclerViewSetup()

        // Handling Item Click
        adapter.listener = object : RecentChatsAdapter.OnChatItemClickListener {
            override fun onChatItemClicked(
                username: String,
                uid: String,
                avatar: String?,
                receiverType: String
            ) {
                navigateToChatActivity(username, uid, avatar, receiverType)
            }
        }

        // Observe The LiveData For Selected Messages
        adapter.getSelectedConversationLiveData().observe(viewLifecycleOwner) { selectedConversations ->
            toggleToolbar(selectedConversations)
            this.selectedConversations = selectedConversations
        }

        // Real-Time Message Listener
        messageListener()
        fetchConversation()
    }

    private fun recyclerViewSetup() {
        adapter = RecentChatsAdapter(requireContext(), emptyList())
        binding.recentChatsRecyclerView.adapter = adapter
        binding.recentChatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun toggleToolbar(selectedConversations: List<Int>) {
        val activity: MainActivity = activity as MainActivity
        val selectConvToolbar: LinearLayout = activity.findViewById(R.id.select_conv_toolbar)
        val whatsappToolbar: LinearLayout = activity.findViewById(R.id.whatsapp_toolbar)
        val selectConvCountTxtView: TextView = activity.findViewById(R.id.selected_conv_count)
        if (selectedConversations.isNotEmpty()){
            selectConvToolbar.visibility = View.VISIBLE
            whatsappToolbar.visibility = View.GONE
        } else {
            selectConvToolbar.visibility = View.GONE
            whatsappToolbar.visibility = View.VISIBLE
        }
        selectConvCountTxtView.text = selectedConversations.size.toString()
    }

    private fun fetchConversation() {
        binding.shimmer.visibility = View.VISIBLE
        binding.recentChatsRecyclerView.visibility = View.GONE
        binding.shimmer.startShimmer()

        conversationsRequest = ConversationsRequestBuilder()
            .setLimit(20)
            .build()

        conversationsRequest.fetchNext(object : CometChat.CallbackListener<List<Conversation>>() {
            override fun onSuccess(list: List<Conversation>?) {
                //Handle List of Conversations
                if (list != null) {
                    stopShimmer()
                    Log.i(tag, "Conversation List: $conversationList")
                    conversationList.clear()
                    conversationList.addAll(list)
                    adapter.setData(conversationList)
                }
            }

            override fun onError(exception: CometChatException?) {
                //Handle Failure
                Log.i(tag, "Fetch Conversation List Failed: $exception")
            }
        })
    }

    private fun stopShimmer() {
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
        binding.recentChatsRecyclerView.visibility = View.VISIBLE
    }

    private fun messageListener() {
        val listenerID = tag
        CometChat.addMessageListener(listenerID, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(textMessage: TextMessage) {
                Log.d(tag, "Realtime text message received successfully: $textMessage")
                // Get the conversation associated with the received message
                val conversation = CometChatHelper.getConversationFromMessage(textMessage)
                // Update the conversation list with the new/updated conversation
                val existingIndex = conversationList.indexOfFirst { it.conversationId == conversation.conversationId }

                if (existingIndex != -1) {
                    // Remove the existing conversation
                    conversationList.removeAt(existingIndex)
                }

                // Add the conversation to the top of the list
                conversationList.add(0, conversation)

                // Update the adapter with the new list
                adapter.setData(conversationList)
            }

            override fun onMediaMessageReceived(mediaMessage: MediaMessage) {
                Log.d(tag, "Media message received successfully: $mediaMessage")
            }

            override fun onCustomMessageReceived(customMessage: CustomMessage) {
                Log.d(tag, "Custom message received successfully: $customMessage")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Fetch Conversations
    }

    private fun navigateToChatActivity(
        name: String,
        uid: String,
        avatar: String? = null,
        receiverType: String
    ) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra(Constants.USER_NAME, name)
        intent.putExtra(Constants.RECEIVER_ID, uid)
        intent.putExtra(Constants.AVATAR, avatar)
        intent.putExtra(Constants.RECEIVER_TYPE, receiverType)
        startActivity(intent)
    }
}