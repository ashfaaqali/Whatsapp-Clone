package com.ali.whatsappplus.ui.fragment
// Ongoing Chats (Launch Screen)
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.data.model.RecentChats
import com.ali.whatsappplus.databinding.FragmentRecentChatsBinding
import com.ali.whatsappplus.ui.activity.ChatActivity
import com.ali.whatsappplus.ui.adapter.RecentChatsAdapter
import com.ali.whatsappplus.viewmodel.RecentChatsViewModel

class RecentChatsFragment : Fragment() {
    private lateinit var binding: FragmentRecentChatsBinding
    private lateinit var viewModel: RecentChatsViewModel
    private lateinit var adapter: RecentChatsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRecentChatsBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this)[RecentChatsViewModel::class.java]

        adapter = RecentChatsAdapter(emptyList())

        adapter.listener = object : RecentChatsAdapter.OnChatItemClickListener {
            override fun onChatItemClicked(chat: RecentChats) {
                navigateToChatActivity(chat)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadRecentChatsFromAssets(requireContext(), "recentChats.json")

        binding.recentChatsRecyclerView.adapter = adapter
        binding.recentChatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.recentChats.observe(viewLifecycleOwner, Observer { recentChats ->
            adapter.setData(recentChats)
        })
    }

    private fun navigateToChatActivity(chat: RecentChats) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("name", chat.contactName)
        startActivity(intent)
    }

}