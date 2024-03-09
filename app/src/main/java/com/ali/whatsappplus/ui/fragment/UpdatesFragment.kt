package com.ali.whatsappplus.ui.fragment
// STORY
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.FragmentUpdatesBinding

class UpdatesFragment : Fragment() {
   private lateinit var binding: FragmentUpdatesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdatesBinding.inflate(layoutInflater)
        return binding.root
    }
}