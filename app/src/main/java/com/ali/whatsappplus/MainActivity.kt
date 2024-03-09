package com.ali.whatsappplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ali.whatsappplus.databinding.ActivityMainBinding
import com.ali.whatsappplus.ui.adapter.MainViewPagerAdapter
import com.ali.whatsappplus.ui.fragment.CallsFragment
import com.ali.whatsappplus.ui.fragment.CommunitiesFragment
import com.ali.whatsappplus.ui.fragment.OngoingChatsFragment
import com.ali.whatsappplus.ui.fragment.UpdatesFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        adapter = MainViewPagerAdapter(supportFragmentManager)

        // Dynamically add tabs
        adapter.addFragment(CommunitiesFragment(), "Communities")
        adapter.addFragment(OngoingChatsFragment(), "Chats")
        adapter.addFragment(UpdatesFragment(), "Updates")
        adapter.addFragment(CallsFragment(), "Calls")
        adapter.notifyDataSetChanged()

        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}