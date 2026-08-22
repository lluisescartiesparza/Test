package com.example.noubasketalzira.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    val id: String,
    val name: String,
    val category: String?,
    @SerialName("created_at") val createdAt: String? = null
)
