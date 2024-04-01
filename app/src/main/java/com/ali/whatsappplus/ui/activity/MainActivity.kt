package com.ali.whatsappplus.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityMainBinding
import com.ali.whatsappplus.ui.adapter.MainViewPagerAdapter
import com.ali.whatsappplus.ui.bottomsheet.LoginBottomSheetDialog
import com.ali.whatsappplus.ui.fragment.CallsFragment
import com.ali.whatsappplus.ui.fragment.RecentChatsFragment
import com.ali.whatsappplus.ui.fragment.UpdatesFragment
import com.ali.whatsappplus.util.Constants
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var user: User
    private var uid = ""
    private val name = "Khal Drogo"
    private lateinit var bottomSheetDialog: LoginBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTabs()
        cometChatInit()
        loginUser("superhero1")

        binding.newChatButton.setOnClickListener {
            navigateToAllContacts()
        }

        binding.options.setOnClickListener {
            showLoginBottomSheet()
        }
    }

    fun logoutAndLogin(uid: String) {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Log.d("Logout", "Logout Successful $p0")
                loginUser(uid)
            }

            override fun onError(p0: CometChatException?) {
                Log.d("Logout", "Logout failed with exception: " + p0?.message)
            }
        })
    }

    private fun showLoginBottomSheet() {
        bottomSheetDialog = LoginBottomSheetDialog()
        bottomSheetDialog.show(supportFragmentManager, "LoginBottomSheet")
    }


    private fun navigateToAllContacts() {
        val intent = Intent(this, ContactsActivity::class.java)
        startActivity(intent)
    }

    private fun loginUser(uid: String) {

        CometChat.login(
            uid,
            Constants.AUTH_KEY,
            object : CometChat.CallbackListener<User>() {
                override fun onSuccess(p0: User?) {
                    Log.d("Login", "Login Successful : " + p0?.toString())
                }

                override fun onError(p0: CometChatException?) {
                    Log.d("Login", "Login failed with exception: " + p0?.message)
                }

            }
        )
    }

    private fun cometChatInit() {

        val appSetting: AppSettings = AppSettings.AppSettingsBuilder()
            .setRegion(Constants.REGION)
            .subscribePresenceForAllUsers()
            .autoEstablishSocketConnection(true)
            .build();

        CometChat.init(
            this,
            Constants.APP_ID,
            appSetting,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) {
                    Log.d("ChatInitSuccess", "Initialization completed successfully")
                }

                override fun onError(p0: CometChatException?) {
                    Log.d("ChatInitFailed", "Initialization failed with exception: " + p0?.message)
                }

            })
    }

    private fun setUpTabs() {
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)

        adapter = MainViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(RecentChatsFragment(), "Chats")
        adapter.addFragment(UpdatesFragment(), "Updates")
        adapter.addFragment(CallsFragment(), "Calls")
        adapter.notifyDataSetChanged()

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}