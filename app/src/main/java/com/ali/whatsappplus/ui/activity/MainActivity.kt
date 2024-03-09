package com.ali.whatsappplus.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.ActivityMainBinding
import com.ali.whatsappplus.ui.adapter.MainViewPagerAdapter
import com.ali.whatsappplus.ui.fragment.CallsFragment
import com.ali.whatsappplus.ui.fragment.RecentChatsFragment
import com.ali.whatsappplus.ui.fragment.UpdatesFragment
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTabs()

        binding.newChatButton.setOnClickListener{
            navigateToAllContacts()
        }
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
        adapter.addFragment(CallsFragment(), "Calls")
        adapter.notifyDataSetChanged()

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}