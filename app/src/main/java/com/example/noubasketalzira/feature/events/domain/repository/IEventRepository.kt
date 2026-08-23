package com.example.noubasketalzira.feature.events.domain.repository

import com.example.noubasketalzira.feature.events.domain.model.Attendance
import com.example.noubasketalzira.feature.events.domain.model.AttendanceStatus
import com.example.noubasketalzira.feature.events.domain.model.Event
import com.example.noubasketalzira.feature.events.domain.model.EventType
import kotlinx.coroutines.flow.Flow

interface IEventRepository {
    fun observeEvents(teamId: String): Flow<List<Event>>
    fun observeAttendance(eventId: String): Flow<List<Attendance>>
    
    suspend fun createEvent(teamId: String, type: EventType, date: Long, description: String?)
    suspend fun deleteEvent(eventId: String)
    suspend fun updateAttendanceStatus(eventId: String, userId: String, status: AttendanceStatus)
    suspend fun markAllAs(eventId: String, status: AttendanceStatus)
    suspend fun syncEvents(teamId: String)
}
