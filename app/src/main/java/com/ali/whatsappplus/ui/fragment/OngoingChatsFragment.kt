package com.ali.whatsappplus.ui.fragment
// Ongoing Chats (Launch Screen)
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.FragmentOngoingChatsBinding

class OngoingChatsFragment : Fragment() {
    private lateinit var binding: FragmentOngoingChatsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOngoingChatsBinding.inflate(layoutInflater)
        return binding.root
    }
}