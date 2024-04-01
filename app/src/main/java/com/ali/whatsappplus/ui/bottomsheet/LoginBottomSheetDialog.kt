package com.ali.whatsappplus.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ali.whatsappplus.databinding.ChatItemBinding
import com.ali.whatsappplus.databinding.SampleLoginBottomSheetBinding
import com.ali.whatsappplus.ui.activity.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoginBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var binding: SampleLoginBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = SampleLoginBottomSheetBinding.inflate(layoutInflater)

        binding.superhero1.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero1")
            dismiss()
        }

        binding.superhero2.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero2")
            dismiss()
        }
        binding.superhero3.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero3")
            dismiss()
        }
        binding.superhero4.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero4")
            dismiss()
        }

        return super.onCreateDialog(savedInstanceState)
    }
}