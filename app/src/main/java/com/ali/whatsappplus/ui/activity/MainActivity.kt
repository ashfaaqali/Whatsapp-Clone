package com.ali.whatsappplus.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityMainBinding
import com.ali.whatsappplus.ui.adapter.MainViewPagerAdapter
import com.ali.whatsappplus.ui.bottomsheet.LoginBottomSheetDialog
import com.ali.whatsappplus.ui.fragment.CallLogFragment
import com.ali.whatsappplus.ui.fragment.RecentChatsFragment
import com.ali.whatsappplus.ui.fragment.UpdatesFragment
import com.ali.whatsappplus.util.Constants
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private var uid = "superhero1"
    private lateinit var bottomSheetDialog: LoginBottomSheetDialog
    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tab Layout And ViewPager Setup
        setUpTabs()

        binding.newChatButton.setOnClickListener {
            navigateToAllContacts()
        }

        // BottomSheet Invocation
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

        adapter.addFragment(RecentChatsFragment(), "Chats")
        adapter.addFragment(UpdatesFragment(), "Updates")
        adapter.addFragment(CallLogFragment(), "Calls")
        adapter.notifyDataSetChanged()

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}