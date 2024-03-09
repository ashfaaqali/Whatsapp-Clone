package com.ali.whatsappplus.ui.fragment
// COMMUNITIES
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.FragmentCommunitiesBinding

class CommunitiesFragment : Fragment() {
   private lateinit var binding: FragmentCommunitiesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunitiesBinding.inflate(layoutInflater)
        return binding.root
    }
}