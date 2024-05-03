package com.ali.whatsappplus.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.whatsappplus.databinding.FragmentCallsBinding
import com.ali.whatsappplus.ui.activity.CallDetailsActivity
import com.ali.whatsappplus.ui.adapter.CallLogAdapter
import com.ali.whatsappplus.util.Constants
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.core.CallLogRequest.CallLogRequestBuilder
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.chat.core.CometChat

class CallLogFragment : Fragment() {
    private val TAG = "CallLogFragment"
    private lateinit var binding: FragmentCallsBinding
    private lateinit var adapter: CallLogAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewSetup()
        adapter.onCallLogItemClick = object : CallLogAdapter.OnCallLogItemClick {
            override fun onCallLogItemClickListener(
                name: String,
                receiverUid: String,
                avatar: String,
                receiverType: String
            ) {
                navigateToCallLogDetailsActivity(name, receiverUid, avatar, receiverType)
            }

        }
    }

    private fun navigateToCallLogDetailsActivity(
        name: String,
        receiverUid: String,
        avatar: String,
        receiverType: String
    ) {
        val intent = Intent(requireContext(), CallDetailsActivity::class.java)
        intent.putExtra(Constants.USER_NAME, name)
        intent.putExtra(Constants.RECEIVER_ID, receiverUid)
        intent.putExtra(Constants.AVATAR, avatar)
        intent.putExtra(Constants.RECEIVER_TYPE, receiverType)
        startActivity(intent)
    }

    private fun fetchCallLogs() {
        val callLogRequest = CallLogRequestBuilder()
            .setAuthToken(CometChat.getUserAuthToken())
            .setLimit(20)
            .setCallCategory(CometChatCallsConstants.CALL_CATEGORY_CALL)
            .build()

        callLogRequest.fetchNext(object : CometChatCalls.CallbackListener<List<CallLog>?>() {
            override fun onSuccess(callLogs: List<CallLog>?) {
                // Handle the fetched call logs
                if (callLogs != null) {
                    adapter.setData(callLogs)
                    Log.i(TAG, "onSuccess callLogRequest: $callLogs")
                }
                stopShimmer()
            }

            override fun onError(e: CometChatException?) {
                // Handle the error
                Log.i(TAG, "onError callLogRequest: $e")
            }
        })
    }

    private fun recyclerViewSetup() {
        adapter = CallLogAdapter(requireContext(), emptyList())
        binding.callLogsRecyclerView.adapter = adapter
        binding.callLogsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        fetchCallLogs()
        binding.shimmer.startShimmer()
    }

    private fun stopShimmer() {
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
    }
}