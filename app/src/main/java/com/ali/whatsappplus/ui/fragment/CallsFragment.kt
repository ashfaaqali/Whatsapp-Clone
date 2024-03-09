package com.ali.whatsappplus.ui.fragment
// VOICE AND VIDEO CALLS
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.FragmentCallsBinding

class CallsFragment : Fragment() {
   private lateinit var binding: FragmentCallsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallsBinding.inflate(layoutInflater)
        return binding.root
    }
}