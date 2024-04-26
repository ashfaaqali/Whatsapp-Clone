package com.ali.whatsappplus.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ali.whatsappplus.databinding.FragmentGroupDetailsBinding

class GroupDetailsFragment: Fragment() {
    private lateinit var binding: FragmentGroupDetailsBinding
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

    }
}