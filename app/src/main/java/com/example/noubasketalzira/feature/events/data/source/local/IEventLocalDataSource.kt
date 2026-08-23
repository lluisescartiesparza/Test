package com.example.noubasketalzira.feature.events.data.source.local

import com.example.noubasketalzira.feature.events.domain.model.Attendance
import com.example.noubasketalzira.feature.events.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface IEventLocalDataSource {
    fun observeEvents(teamId: String): Flow<List<Event>>
    fun observeAttendance(eventId: String): Flow<List<Attendance>>
    suspend fun insertEvent(event: Event)
    suspend fun insertAttendances(attendances: List<Attendance>)
    suspend fun deleteEvent(eventId: String)
    suspend fun updateAttendanceStatus(eventId: String, userId: String, status: String)
    suspend fun updateAllAttendanceStatus(eventId: String, status: String)
    suspend fun getPlayersForTeam(teamId: String): List<String> // Returns userIds of players
}
