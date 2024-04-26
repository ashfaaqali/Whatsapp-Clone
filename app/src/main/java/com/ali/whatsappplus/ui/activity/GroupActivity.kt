package com.ali.whatsappplus.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ali.whatsappplus.R
import com.ali.whatsappplus.ui.fragment.GroupDetailsFragment
import com.ali.whatsappplus.ui.fragment.SelectGroupMembersFragment
import com.ali.whatsappplus.util.Constants.FRAGMENT_GROUP_DETAILS
import com.ali.whatsappplus.util.Constants.FRAGMENT_SELECT_GROUP_MEMBERS
import com.ali.whatsappplus.util.Constants.FRAGMENT_TO_LOAD

class GroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)
        val fragmentToLoad = intent.getStringExtra(FRAGMENT_TO_LOAD)

        val selectedGroupMembers = intent.getStringArrayListExtra("selected_members")

        if (fragmentToLoad == FRAGMENT_SELECT_GROUP_MEMBERS)
            startFragment(SelectGroupMembersFragment())
        if (fragmentToLoad == FRAGMENT_GROUP_DETAILS) {
            val args = Bundle()
            args.putStringArrayList("selected_members", selectedGroupMembers)
            startFragment(GroupDetailsFragment(), args)
        }
    }

    private fun startFragment(fragment: Fragment, args: Bundle? = null) {
        if (args != null) {
            fragment.arguments = args
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                .commit()
        }
    }


}