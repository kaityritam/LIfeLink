package com.lifelink.lifelink.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("blood_group")
    val bloodGroup: String? = null,
    @SerialName("is_email_verified")
    val isEmailVerified: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Profile(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null
)