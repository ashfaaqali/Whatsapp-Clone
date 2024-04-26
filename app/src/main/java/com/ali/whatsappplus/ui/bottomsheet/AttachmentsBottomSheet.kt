package com.ali.whatsappplus.ui.bottomsheet

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Dialog
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.ali.whatsappplus.R
import com.ali.whatsappplus.databinding.AttachmentsBottomSheetBinding
import com.ali.whatsappplus.ui.activity.ChatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AttachmentsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: AttachmentsBottomSheetBinding
    private var TAG = "AttachmentsBottomSheet"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    // Register ActivityResult handler
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            Log.d(TAG, "Permission Result: $results")
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        binding = AttachmentsBottomSheetBinding.inflate(layoutInflater)

        binding.gallery.setOnClickListener {
            if (!isMediaPermissionGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) requestPermissions.launch(
                    arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
                )
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestPermissions.launch(
                    arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
                )
                else requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
            } else {
                (activity as? ChatActivity)?.openImagePicker()
                dismiss()
            }
        }

        binding.contacts.setOnClickListener {
            Toast.makeText(context, "Send Contacts", Toast.LENGTH_SHORT).show()
        }

        binding.document.setOnClickListener {
            Toast.makeText(context, "Send Documents", Toast.LENGTH_SHORT).show()
        }

        binding.location.setOnClickListener {
            Toast.makeText(context, "Send Location", Toast.LENGTH_SHORT).show()
        }

        binding.audio.setOnClickListener {
            Toast.makeText(context, "Send Audio", Toast.LENGTH_SHORT).show()
        }

        binding.camera.setOnClickListener {
            Toast.makeText(context, "Click Picture", Toast.LENGTH_SHORT).show()
        }


        return dialog
    }

    // Checks If A Permission Is Granted And Returns Boolean
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun isMediaPermissionGranted(): Boolean {

        // Permissions
        val readExternalStoragePermission =
            ContextCompat.checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE)
        val readMediaImagesPermission =
            ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES)
        val readMediaVideoPermission =
            ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_VIDEO)
        val readUserSelectedMediaPermission =
            ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_VISUAL_USER_SELECTED)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (readMediaImagesPermission == PERMISSION_GRANTED || readMediaVideoPermission == PERMISSION_GRANTED)) {
            true
        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && readUserSelectedMediaPermission == PERMISSION_GRANTED) {
            true
        } else if (readExternalStoragePermission == PERMISSION_GRANTED) {
            true
        } else {
            false
        }
    }

    override fun getTheme() = R.style.Theme_BottomSheetDialog
}