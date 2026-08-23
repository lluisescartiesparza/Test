package com.example.noubasketalzira.feature.events.data.source.remote

import com.example.noubasketalzira.feature.events.data.source.remote.dto.AttendanceInsertDto
import com.example.noubasketalzira.feature.events.data.source.remote.dto.EventInsertDto

interface IEventRemoteDataSource {
    suspend fun fetchEvents(teamId: String): List<EventInsertDto>
    suspend fun fetchAttendance(eventId: String): List<AttendanceInsertDto>
}
