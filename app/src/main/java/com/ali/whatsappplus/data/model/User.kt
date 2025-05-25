package com.ali.whatsappplus.data.model

import org.json.JSONObject

data class User(
    val uid: String,
    val name: String,
    val avatar: String,
    val link: String,
    val role: String,
    val metadata: JSONObject,
    val status: String,
    val statusMessage: String,
    val lastActiveAt: Long,
    val hasBlockedMe: Boolean,
    val blockedByMe: Boolean,
    val tags: List<String>,
    val deactivatedAt: Long
)

