package com.ali.whatsappplus.ui.views

import android.content.Context
import android.util.AttributeSet
import com.ali.whatsappplus.R
import com.google.android.material.card.MaterialCardView

class SendButton @JvmOverloads constructor(context: Context, attr: AttributeSet, defStyleAttr: Int = 0) : MaterialCardView(context, attr, defStyleAttr) {

    init {
        applyDefaultStyle()
        setOnClickListener {
            // Handle send button click
        }
    }

    private fun applyDefaultStyle() {
        val typedArray = context.theme.obtainStyledAttributes(
            null,
            R.styleable.SendButton,
            0,
            0
        )
        try {
            setBackgroundResource(R.drawable.ic_send)
//            setBackgroundColor(typedArray.getColor())
        } finally {
            typedArray.recycle()
        }

    }


    fun setSendButtonVisibility(isVisible: Boolean) {
        visibility = if (isVisible) {
            VISIBLE
        } else {
            GONE
        }
    }
}