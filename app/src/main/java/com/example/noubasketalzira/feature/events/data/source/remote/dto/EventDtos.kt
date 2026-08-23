package com.example.noubasketalzira.feature.events.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventInsertDto(val id: String, val team_id: String, val type: String, val date: String, val description: String?)

@Serializable
data class AttendanceInsertDto(val event_id: String, val user_id: String, val status: String)

@Serializable
data class AttendanceUpdateDto(val status: String)
