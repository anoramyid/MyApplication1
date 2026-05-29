package com.example.myapplication1.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ConnectionGroup(
    val id: String? = null,
    val user_id: String,
    val name: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class SshConnection(
    val id: String? = null,
    val user_id: String,
    val name: String,
    val host: String,
    val username: String,
    val password: String,
    val port: Int = 22,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
