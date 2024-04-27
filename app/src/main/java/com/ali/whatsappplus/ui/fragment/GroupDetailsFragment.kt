package com.ali.whatsappplus.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.FragmentGroupDetailsBinding
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.CreateGroupWithMembersListener
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.helpers.Logger
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import java.util.HashMap
import kotlin.random.Random

class GroupDetailsFragment : Fragment() {

    private var groupMembers: MutableList<GroupMember> = ArrayList()
    private lateinit var binding: FragmentGroupDetailsBinding
    private val tag = "GroupDetailsFragment"
    private var loggedInUser = CometChat.getLoggedInUser()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val selectedMembers = arguments?.getStringArrayList("selected_members")
        Log.i("GroupDetailsFragment", selectedMembers.toString())

        // Iterate over the StringArrayList & add as Group Member object in the list
        if (selectedMembers != null) {
            for (member in selectedMembers) {
                groupMembers.add(GroupMember(member, CometChatConstants.SCOPE_PARTICIPANT))
            }
            // Add the currently logged in user as the Admin
            groupMembers.add(GroupMember(loggedInUser.uid, CometChatConstants.SCOPE_ADMIN))
        }

        binding.groupPrivacyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.protected_radio_btn)
                binding.passwordEditText.visibility = View.VISIBLE
            else binding.passwordEditText.visibility = View.GONE
        }

        binding.checkMarkFab.setOnClickListener {
            val groupName = binding.groupNameEditText.text.toString()
            val passwordEditText = binding.passwordEditText
            val groupPassword = passwordEditText.text.toString()
            val selectedGroupTypeRadioBtnId = binding.groupPrivacyRadioGroup.checkedRadioButtonId
            val selectedGroupType = view.findViewById<RadioButton>(selectedGroupTypeRadioBtnId).text

            if (isValid(groupName, passwordEditText, groupPassword))
                createGroupWithMembers(groupName, groupPassword, selectedGroupType, groupMembers)
        }


    }

    private fun showToast(field: String) {
        Toast.makeText(
            requireContext(),
            "$field can't be empty",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun isValid(
        groupName: String,
        passwordEditText: EditText,
        groupPassword: String
    ): Boolean {
        return if (groupName.isEmpty()) {
            showToast("Group name")
            false
        } else if (passwordEditText.isVisible && groupPassword.isEmpty()) {
            showToast("Group password")
            false
        } else true
    }

    private fun createGroupWithMembers(
        groupName: String,
        groupPassword: String?,
        selectedGroupType: CharSequence,
        selectedMembers: MutableList<GroupMember>
    ) {
        val groupType = getGroupType(selectedGroupType)
        val guid = getGuid()
        val group = Group(guid, groupName, groupType, groupPassword)

        Log.i(tag, "createGroupWithMembers Group: $group")

        CometChat.createGroupWithMembers(
            group,
            selectedMembers,
            null,
            object : CreateGroupWithMembersListener() {
                override fun onSuccess(p0: Group?, p1: HashMap<String, String>?) {
                    Log.i(tag, "createGroupWithMembers onSuccess: $p0, $p1")
                    Logger.error(p0.toString(), p1.toString())
                }

                override fun onError(p0: CometChatException?) {
                    // onError
                }

            })
    }

    private fun getGuid(): String {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val random = Random.Default
        val builder = StringBuilder(10)
        val generatedChars = mutableSetOf<Char>()

        while (builder.length < 10) {
            val randomChar = charPool.random(random)
            if (generatedChars.add(randomChar)) { // Add only if not already present
                builder.append(randomChar)
            }
        }

        return builder.toString()
    }


    private fun getGroupType(selectedGroupType: CharSequence): String {
        return when (selectedGroupType) {
            CometChatConstants.GROUP_TYPE_PASSWORD -> CometChatConstants.GROUP_TYPE_PASSWORD
            CometChatConstants.GROUP_TYPE_PRIVATE -> CometChatConstants.GROUP_TYPE_PRIVATE
            else -> CometChatConstants.GROUP_TYPE_PUBLIC
        }

    }
}