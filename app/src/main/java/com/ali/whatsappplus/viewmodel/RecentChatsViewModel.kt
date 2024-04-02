package com.ali.whatsappplus.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ali.whatsappplus.data.model.RecentChats
import org.json.JSONObject

class RecentChatsViewModel() : ViewModel() {

    private val _recentChats = MutableLiveData<List<RecentChats>>()
    val recentChats: LiveData<List<RecentChats>> = _recentChats

    fun loadRecentChatsFromAssets(context: Context, fileName: String) {
        val jsonString = readJsonFromAssets(context, fileName)
        val chatsArray = JSONObject(jsonString).getJSONArray("chats")
        val recentChatsList = mutableListOf<RecentChats>()
        for (i in 0 until chatsArray.length()) {
            val chatObject = chatsArray.getJSONObject(i)
            val recentChat = RecentChats(
                id = chatObject.getInt("id"),
                contactName = chatObject.getString("contact_name"),
                lastMessage = chatObject.getString("last_message"),
                timestamp = chatObject.getString("timestamp")
            )
            recentChatsList.add(recentChat)
        }
        _recentChats.value = recentChatsList
    }

    private fun readJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use {
            it.readText()
        }
    }
}