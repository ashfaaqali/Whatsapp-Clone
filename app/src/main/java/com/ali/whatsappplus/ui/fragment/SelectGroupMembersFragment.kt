package com.ali.whatsappplus.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.databinding.FragmentSelectGroupMembersBinding
import com.ali.whatsappplus.ui.activity.GroupActivity
import com.ali.whatsappplus.ui.adapter.SelectGroupMembersAdapter
import com.ali.whatsappplus.util.Constants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User

class SelectGroupMembersFragment : Fragment() {
    private lateinit var binding: FragmentSelectGroupMembersBinding
    private lateinit var adapter: SelectGroupMembersAdapter
    private val tag = "SelectGroupMembers"
    private var selectedMembers: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectGroupMembersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewSetup()
        fetchUsers()

        adapter.selectedMembersLiveData().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.fab.visibility = View.VISIBLE
                selectedMembers = it
            } else binding.fab.visibility = View.GONE
        }

        binding.fab.setOnClickListener {
            if (selectedMembers.isNotEmpty()) navigateToGroupDetails()
            else showToast()
        }

        binding.back.setOnClickListener {
            activity?.finish()
        }
    }

    private fun navigateToGroupDetails() {
        val intent = Intent(requireContext(), GroupActivity::class.java)
        intent.putExtra(Constants.FRAGMENT_TO_LOAD, Constants.FRAGMENT_GROUP_DETAILS)
        intent.putStringArrayListExtra("selected_members", ArrayList(selectedMembers))
        startActivity(intent)
        activity?.finish()
    }

    private fun handleBackPress() {
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        })
    }

    private fun showToast() {
        Toast.makeText(
            requireContext(),
            "At least 1 member must be selected",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        binding.shimmer.startShimmer()
    }

    private fun stopShimmer() {
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
    }

    private fun fetchUsers() {
        // Request For Users
        val usersRequest = UsersRequest.UsersRequestBuilder()
            .setLimit(30)
            .build()

        usersRequest.fetchNext(object : CometChat.CallbackListener<List<User>>() {
            override fun onSuccess(userList: List<User>?) {
                if (userList != null) {
                    stopShimmer()
                    adapter.setData(userList)
                    Log.i(tag, "User List: $userList")
                }
            }

            override fun onError(p0: CometChatException?) {
                Log.i(tag, "$p0")
            }
        })
    }

    private fun recyclerViewSetup() {
        adapter = SelectGroupMembersAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

}