//package com.ali.whatsappplus.ui.conversation
//
//import androidx.lifecycle.ViewModel
//import com.ali.whatsappplus.data.remote.ApiService
//import com.ali.whatsappplus.data.repository.ConversationsRepository
//
//class ConversationsViewModel(private val conversationsRepository: ConversationsRepository) : ViewModel() {
//    suspend fun resetUserConversation(uid: String, apiListener: ApiService.ApiListener) {
//        conversationsRepository.resetUserConversation(uid, apiListener)
//    }
//}