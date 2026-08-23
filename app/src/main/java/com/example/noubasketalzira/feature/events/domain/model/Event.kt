package com.example.noubasketalzira.feature.events.domain.model

data class Event(
    val id: String,
    val teamId: String,
    val type: EventType,
    val date: Long,
    val description: String?
)
