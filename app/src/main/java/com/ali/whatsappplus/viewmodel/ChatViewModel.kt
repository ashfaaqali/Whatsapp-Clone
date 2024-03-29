package com.ali.whatsappplus.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ali.whatsappplus.data.model.Message
import com.ali.whatsappplus.util.Constants
import kotlinx.coroutines.launch

class ChatViewModel(): ViewModel() {

    private val _message = MutableLiveData<List<Message>>()
    val message: LiveData<List<Message>> = _message

    fun addToChat(message: String, source: String){
        val messageList = mutableListOf<Message>()
        viewModelScope.launch {
            val message = Message(
                message = message,
                source = source
            )
            messageList.add(message)
            _message.value = messageList
        }
    }

    fun addResponse(response: String){
        _message.value = message.value?.dropLast(1)
        addToChat(response, Constants.RECEIVED)
    }
}