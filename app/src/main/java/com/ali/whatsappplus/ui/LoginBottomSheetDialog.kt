package com.ali.whatsappplus.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ali.whatsappplus.CometChat
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.SampleLoginBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoginBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: SampleLoginBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var progressDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SampleLoginBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button click listeners
        setClickListener(binding.user1, "ashfaq-ali")
        setClickListener(binding.user2, "ashfaq-a")
        setClickListener(binding.user3, "cometchat-uid-3")
        setClickListener(binding.user4, "cometchat-uid-4")
    }

    private fun setClickListener(view: View, userId: String) {
        view.setOnClickListener {
            showProgressDialog()
            CometChat.logoutAndLogin(userId)
            progressDialog?.dismiss()
            dismiss()
        }
    }

    private fun showProgressDialog() {
        val dialogView = layoutInflater.inflate(R.layout.progress_bar, null).apply {
            findViewById<TextView>(R.id.text).text = getString(R.string.switching_account)
        }

        progressDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()
            .apply { show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        progressDialog?.dismiss()
    }
}