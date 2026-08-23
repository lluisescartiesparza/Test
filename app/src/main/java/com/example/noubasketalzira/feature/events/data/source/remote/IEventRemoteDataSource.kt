package com.example.noubasketalzira.feature.events.data.source.remote

import com.example.noubasketalzira.feature.events.data.worker.AttendanceInsertDto
import com.example.noubasketalzira.feature.events.data.worker.EventInsertDto

interface IEventRemoteDataSource {
    suspend fun fetchEvents(teamId: String): List<EventInsertDto>
    suspend fun fetchAttendance(eventId: String): List<AttendanceInsertDto>
}
