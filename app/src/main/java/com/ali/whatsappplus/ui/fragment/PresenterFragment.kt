package com.ali.whatsappplus.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ali.whatsappplus.databinding.FragmentPresenterBinding
import com.ali.whatsappplus.util.Constants
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.listeners.CometChatCallsEventsListener
import com.cometchat.calls.model.AudioMode
import com.cometchat.calls.model.CallSwitchRequestInfo
import com.cometchat.calls.model.RTCMutedUser
import com.cometchat.calls.model.RTCRecordingInfo
import com.cometchat.calls.model.RTCUser

class PresenterFragment : Fragment() {
    private lateinit var binding: FragmentPresenterBinding
    private var isPresenter: Boolean = false
    private val TAG = "PresenterFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPresenterBinding.inflate(layoutInflater)
        if (arguments!=null)
            isPresenter = requireArguments().getBoolean(Constants.IS_PRESENTER)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: Presenter Started")
        startPresenter()
    }

    override fun onResume() {
        super.onResume()
        val listenerId = "UNIQUE_LISTENER_ID"

        CometChatCalls.addCallsEventListeners(listenerId, object : CometChatCallsEventsListener {
            override fun onCallEnded() {
                Log.e(TAG, ">>>:  onCallEnded()")
            }

            override fun onCallEndButtonPressed() {
                Log.e(TAG, ">>>:  onCallEndButtonPressed()")
            }

            override fun onUserJoined(user: RTCUser) {
                Log.e(TAG, ">>>:  onUserJoined()")
            }

            override fun onUserLeft(user: RTCUser) {
                Log.e(TAG, ">>>:  onUserLeft()")
            }

            override fun onUserListChanged(users: ArrayList<RTCUser>) {
                Log.e(TAG, ">>>:  onUserListChanged()")
            }

            override fun onAudioModeChanged(devices: ArrayList<AudioMode?>?) {
                Log.e(TAG, ">>>:  onAudioModeChanged()")
            }

            override fun onCallSwitchedToVideo(info: CallSwitchRequestInfo) {
                Log.e(TAG, ">>>:  onCallSwitchedToVideo()")
            }

            override fun onUserMuted(muteObj: RTCMutedUser) {
                Log.e(TAG, ">>>:  onUserMuted()")
            }

            override fun onRecordingToggled(info: RTCRecordingInfo) {
                Log.e(TAG, ">>>:  onRecordingToggled()")
            }

            override fun onError(p0: com.cometchat.calls.exceptions.CometChatException?) {
                Log.e(TAG, ">>>:  onError()")
            }
        })
    }

    private fun startPresenter() {
        val videoContainer = binding.videoContainer
        val activityContext = requireActivity() //Your activity reference

        val presenterSettings = CometChatCalls.PresentationSettingsBuilder(activityContext)
            .setDefaultLayoutEnable(true)
            .setIsPresenter(isPresenter)
            .setEventListener(object : CometChatCallsEventsListener {
                override fun onCallEnded() {}
                override fun onCallEndButtonPressed() {}
                override fun onUserJoined(user: RTCUser) {}
                override fun onUserLeft(user: RTCUser) {}
                override fun onUserListChanged(users: ArrayList<RTCUser>) {}
                override fun onAudioModeChanged(devices: ArrayList<AudioMode?>?) {}
                override fun onCallSwitchedToVideo(callSwitchRequestInfo: CallSwitchRequestInfo) {}
                override fun onUserMuted(muteObj: RTCMutedUser) {}
                override fun onRecordingToggled(recordingInfo: RTCRecordingInfo) {}
                override fun onError(e: com.cometchat.calls.exceptions.CometChatException) {}
            })
            .build()

        val callToken = arguments?.getString(Constants.CALL_TOKEN)
        CometChatCalls.joinPresentation(
            callToken,
            presenterSettings,
            videoContainer,
            object : CometChatCalls.CallbackListener<String?>() {
                override fun onSuccess(s: String?) {
                    Log.e(TAG, "startSession onSuccess: $s")
                }

                override fun onError(e: com.cometchat.calls.exceptions.CometChatException) {
                    Log.e(TAG, "CallSDKLog >>>: startSession onError: $e")
                }
            })

    }
}