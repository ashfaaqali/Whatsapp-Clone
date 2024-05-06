package com.ali.whatsappplus.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.ali.whatsappplus.Application
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityMainBinding
import com.ali.whatsappplus.ui.adapter.MainViewPagerAdapter
import com.ali.whatsappplus.ui.bottomsheet.LoginBottomSheetDialog
import com.ali.whatsappplus.ui.fragment.CallLogFragment
import com.ali.whatsappplus.ui.fragment.RecentChatsFragment
import com.ali.whatsappplus.ui.fragment.UpdatesFragment
import com.ali.whatsappplus.util.Constants
import com.cometchat.calls.model.CallGroup
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.helpers.Logger
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private var uid = "superhero1"
    private val listenerId = "CallListener"
    private lateinit var bottomSheetDialog: LoginBottomSheetDialog
    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val myApplication: Application = (application as Application)
        myApplication.setCurrentActivity(this)
        // Logger.enableLogs("221089")

        setUpTabs() // Tab Layout And ViewPager Setup

        // All Contacts/ New Chat Floating Action Button
        binding.newChatButton.setOnClickListener {
            navigateToAllContacts() // Intent to ContactsActivity
        }

        // BottomSheet Invocation
        binding.options.setOnClickListener {
            showLoginBottomSheet()
        }

        // Camera Button
        binding.icCamera.setOnClickListener {
            showToast("Camera")
        }

        binding.search.setOnClickListener {
            showToast("Search")
        }
    }

    override fun onResume() {
        super.onResume()
        callListeners() // Listener for incoming, outgoing, call accept, call reject.
    }

    override fun onPause() {
        super.onPause()
        // Remove call listener when not in use.
        CometChat.removeCallListener(listenerId)
    }

    private fun callListeners() { // Listener for incoming, outgoing, call accept, call reject.

        CometChat.addCallListener(listenerId,object : CometChat.CallListener(){
            override fun onOutgoingCallAccepted(p0: Call?) {
                Log.d(TAG, "Outgoing call accepted: " + p0?.toString())
            }

            override fun onIncomingCallReceived(call: Call?) {
                Log.d(TAG, "Incoming call: " + call?.toString())
                if (call != null){
                    val receiverType = call.receiverType
                    val intent = Intent(this@MainActivity, VoiceCall::class.java)

                    if (receiverType == CometChatConstants.RECEIVER_TYPE_USER){
                        intent.putExtra(Constants.USER_NAME, call.sender.name)
                        intent.putExtra(Constants.AVATAR, call.sender.avatar)
                        intent.putExtra(Constants.RECEIVER_ID, call.sender.uid)
                        intent.putExtra(Constants.RECEIVER_TYPE, call.receiverType)
                        startActivity(intent)
                    } else {
                        intent.putExtra(Constants.USER_NAME, call.sender.name)
                        intent.putExtra(Constants.AVATAR, call.sender.avatar)
                        intent.putExtra(Constants.RECEIVER_ID, call.sender.uid)
                        intent.putExtra(Constants.RECEIVER_TYPE, call.receiverType)
                        startActivity(intent)
                    }
                }
            }

            override fun onIncomingCallCancelled(p0: Call?) {
                Log.d(TAG, "Incoming call cancelled: " + p0?.toString())
            }

            override fun onOutgoingCallRejected(p0: Call?) {
                Log.d(TAG, "Outgoing call rejected: " + p0?.toString())
            }

            override fun onCallEndedMessageReceived(p0: Call?) {
                Log.d(TAG, "End call message received: " + p0?.toString())
            }

        })
    }

    private fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    private fun showLoginBottomSheet() { // Show sample accounts BottomSheetDialog
        bottomSheetDialog = LoginBottomSheetDialog()
        bottomSheetDialog.show(supportFragmentManager, Constants.LOGIN_BOTTOM_SHEET_TAG)
    }

    private fun navigateToAllContacts() { // Intent to ContactsActivity
        val intent = Intent(this, ContactsActivity::class.java)
        startActivity(intent)
    }

    private fun setUpTabs() { // TabLayout setup with ViewPager
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        adapter = MainViewPagerAdapter(supportFragmentManager)

        // Add Tabs
        adapter.addFragment(RecentChatsFragment(), "Chats")
        adapter.addFragment(UpdatesFragment(), "Updates")
        adapter.addFragment(CallLogFragment(), "Calls")
        adapter.notifyDataSetChanged()

        // Set the adapter for ViewPager
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}