package com.ali.whatsappplus.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ali.whatsappplus.Application
import com.ali.whatsappplus.databinding.SampleLoginBottomSheetBinding
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
        val application = requireActivity().application as Application

        binding = SampleLoginBottomSheetBinding.inflate(layoutInflater)

        binding.superhero1.setOnClickListener{
            application.logoutAndLogin("superhero1")
            dismiss()
        }

        binding.superhero2.setOnClickListener{
            application.logoutAndLogin("superhero2")
            dismiss()
        }

        binding.superhero3.setOnClickListener{
            application.logoutAndLogin("superhero3")
            dismiss()
        }

        binding.superhero4.setOnClickListener{
            application.logoutAndLogin("superhero4")
            dismiss()
        }

        return super.onCreateDialog(savedInstanceState)
    }
}