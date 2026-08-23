package com.example.noubasketalzira.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamMemberDto(
    @SerialName("team_id") val teamId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("role") val role: String
)
