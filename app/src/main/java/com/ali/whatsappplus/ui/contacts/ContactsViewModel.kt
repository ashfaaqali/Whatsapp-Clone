package com.ali.whatsappplus.ui.contacts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ali.whatsappplus.data.model.AllContacts
import org.json.JSONObject

class ContactsViewModel() : ViewModel() {

    private val _allContacts = MutableLiveData<List<AllContacts>>()
    val allContacts: LiveData<List<AllContacts>> = _allContacts


}