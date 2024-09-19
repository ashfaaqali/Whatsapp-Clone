package com.ali.whatsappplus.ui.bottomsheet

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ali.whatsappplus.Application
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.SampleLoginBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoginBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var binding: SampleLoginBottomSheetBinding
    private var dialog: AlertDialog? = null
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

        binding.superhero1.setOnClickListener {
            showProgressBar()
            application.logoutAndLogin("cometchat-uid-1")
            dismiss()
        }

        binding.superhero2.setOnClickListener {
            showProgressBar()
            application.logoutAndLogin("cometchat-uid-2")
            dismiss()
        }

        binding.superhero3.setOnClickListener {
            showProgressBar()
            application.logoutAndLogin("cometchat-uid-3")
            dismiss()
        }

        binding.superhero4.setOnClickListener {
            showProgressBar()
            application.logoutAndLogin("cometchat-uid-4")
            dismiss()
        }

        return super.onCreateDialog(savedInstanceState)
    }

    private fun showProgressBar() {
        // Inflate the custom view
        val dialogView = layoutInflater.inflate(R.layout.progress_bar, null)

        // Find the views in the custom layout
        val textView: TextView = dialogView.findViewById(R.id.text)
        textView.text = "Switching account..."

        // Create the AlertDialog
        dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog?.show()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog?.dismiss()
            dialog = null
        }, 4000)
    }
}