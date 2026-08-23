package com.example.noubasketalzira.feature.events.domain.model

data class Attendance(
    val eventId: String,
    val userId: String,
    val userName: String, // Useful for UI
    val status: AttendanceStatus
)
