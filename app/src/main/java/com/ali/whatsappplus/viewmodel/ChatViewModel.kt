package com.ali.whatsappplus.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ali.whatsappplus.data.model.Message
import com.ali.whatsappplus.util.Constants
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList = mutableListOf<Message>()

    fun addToChat(textMessage: String, source: String) {
        viewModelScope.launch {
            val message = Message(
                message = textMessage,
                source = source
            )
            messageList.add(message)
        }
    }

    fun addResponse(response: String) {
        addToChat(response, Constants.RECEIVED)
    }
}