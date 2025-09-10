package com.lifelink.lifelink.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BloodRequest(
    @SerialName("id") val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("blood_type") val bloodType: String,
    @SerialName("hospital_name") val hospitalName: String,
    @SerialName("urgency") val urgency: String,
    @SerialName("details") val details: String,
    @SerialName("status") var status: String = "Pending",
    @SerialName("donor_id") val donorId: String? = null
)