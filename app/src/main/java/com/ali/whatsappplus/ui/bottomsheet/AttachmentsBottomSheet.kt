package com.ali.whatsappplus.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.AttachmentsBottomSheetBinding
import com.ali.whatsappplus.databinding.ChatItemBinding
import com.ali.whatsappplus.databinding.SampleLoginBottomSheetBinding
import com.ali.whatsappplus.ui.activity.ChatActivity
import com.ali.whatsappplus.ui.activity.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AttachmentsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: AttachmentsBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        binding = AttachmentsBottomSheetBinding.inflate(layoutInflater)

        binding.document.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero1")
            dismiss()
        }

        binding.camera.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero2")
            dismiss()
        }

        binding.gallery.setOnClickListener{
            (activity as? ChatActivity)?.openImagePicker()
            dismiss()
        }

        binding.audio.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero4")
            dismiss()
        }

        binding.location.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero4")
            dismiss()
        }

        binding.contacts.setOnClickListener{
            (activity as? MainActivity)?.logoutAndLogin("superhero4")
            dismiss()
        }

        return dialog
    }

    override fun getTheme() = R.style.Theme_BottomSheetDialog
}