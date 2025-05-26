package com.ali.whatsappplus.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.ali.whatsappplus.CometChat
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityMainBinding
import com.ali.whatsappplus.ui.LoginBottomSheetDialog
import com.ali.whatsappplus.ui.call.calllog.CallLogFragment
import com.ali.whatsappplus.ui.contacts.ContactsActivity
import com.ali.whatsappplus.ui.conversation.ConversationsFragment
import com.ali.whatsappplus.ui.updates.UpdatesFragment
import com.ali.whatsappplus.common.util.Constants
import com.ali.whatsappplus.common.util.Constants.DEFAULT_USER_ID
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private val listenerId = "CallListener"
    private lateinit var bottomSheetDialog: LoginBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CometChat.login(uid = DEFAULT_USER_ID, onSuccess = {

            CometChat.registerCallListeners(this)
            CometChat.registerPushToken(this)

        }, onError = {
            Log.e(TAG, "Login error: ${it?.message}")
        })
        setUpTabs()
        createNotificationChannel()


        binding.newChatButton.setOnClickListener {
            navigateToAllContacts()
        }

        binding.options.setOnClickListener {
            showLoginBottomSheet()
        }

        binding.icCamera.setOnClickListener {
            showToast("Camera")
        }

        binding.search.setOnClickListener {
            showToast("Search")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    private fun showLoginBottomSheet() {
        bottomSheetDialog = LoginBottomSheetDialog()
        bottomSheetDialog.show(supportFragmentManager, Constants.LOGIN_BOTTOM_SHEET_TAG)
    }

    private fun navigateToAllContacts() {
        val intent = Intent(this, ContactsActivity::class.java)
        startActivity(intent)
    }

    private fun setUpTabs() {
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        adapter = MainViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(ConversationsFragment(), "Chats")
        adapter.addFragment(UpdatesFragment(), "Updates")
        adapter.addFragment(CallLogFragment(), "Calls")
        adapter.notifyDataSetChanged()

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}