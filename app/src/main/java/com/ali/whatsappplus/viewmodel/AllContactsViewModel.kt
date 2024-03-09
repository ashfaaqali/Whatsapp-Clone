package com.ali.whatsappplus.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ali.whatsappplus.data.model.AllContacts
import org.json.JSONObject

class AllContactsViewModel() : ViewModel() {

    private val _allContacts = MutableLiveData<List<AllContacts>>()
    val allContacts: LiveData<List<AllContacts>> = _allContacts

    fun loadRecentContactsFromAssets(context: Context, fileName: String) {
        val jsonString = readJsonFromAssets(context, fileName)
        val contactsArray = JSONObject(jsonString).getJSONArray("contacts")
        val allContactsList = mutableListOf<AllContacts>()
        for (i in 0 until contactsArray.length()) {
            val contactObject = contactsArray.getJSONObject(i)
            val allContacts = AllContacts(
                id = contactObject.getInt("id"),
                name = contactObject.getString("name"),
                status = contactObject.getString("status"),
                )
            allContactsList.add(allContacts)
        }
        _allContacts.value = allContactsList
    }

    private fun readJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use {
            it.readText()
        }
    }
}