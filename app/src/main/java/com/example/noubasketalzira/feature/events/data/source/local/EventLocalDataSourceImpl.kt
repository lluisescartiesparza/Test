package com.example.noubasketalzira.feature.events.data.source.local

import com.example.noubasketalzira.core.data.local.dao.AttendanceDao
import com.example.noubasketalzira.core.data.local.dao.EventDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.dao.UserDao
import com.example.noubasketalzira.core.data.local.entity.AttendanceEntity
import com.example.noubasketalzira.core.data.local.entity.EventEntity
import com.example.noubasketalzira.core.data.local.entity.toDomain
import com.example.noubasketalzira.feature.events.domain.model.Attendance
import com.example.noubasketalzira.feature.events.domain.model.AttendanceStatus
import com.example.noubasketalzira.feature.events.domain.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventLocalDataSourceImpl(
    private val eventDao: EventDao,
    private val attendanceDao: AttendanceDao,
    private val teamMemberDao: TeamMemberDao,
    private val userDao: UserDao
) : IEventLocalDataSource {

    override fun observeEvents(teamId: String): Flow<List<Event>> {
        return eventDao.observeEventsByTeam(teamId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeAttendance(eventId: String): Flow<List<Attendance>> {
        return attendanceDao.observeAllTeamAttendances(eventId).map { items ->
            items.map { item ->
                Attendance(
                    eventId = item.eventId,
                    userId = item.userId,
                    userName = item.userName,
                    status = AttendanceStatus.valueOf(item.status)
                )
            }
        }
    }

    override suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(
            EventEntity(
                id = event.id,
                teamId = event.teamId,
                type = event.type,
                date = event.date,
                description = event.description,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun insertAttendances(attendances: List<Attendance>) {
        attendanceDao.insertAttendances(attendances.map {
            AttendanceEntity(
                eventId = it.eventId,
                userId = it.userId,
                status = it.status.name,
                createdAt = System.currentTimeMillis()
            )
        })
    }

    override suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEvent(eventId)
    }

    override suspend fun updateAttendanceStatus(eventId: String, userId: String, status: String) {
        attendanceDao.insertAttendance(
            AttendanceEntity(
                eventId = eventId,
                userId = userId,
                status = status,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateAllAttendanceStatus(eventId: String, status: String) {
        attendanceDao.updateAllAttendanceStatus(eventId, status)
    }

    override suspend fun getPlayersForTeam(teamId: String): List<String> {
        return teamMemberDao.getMembersByTeamIdAndRole(teamId, "JUGADOR").map { it.userId }
    }
}
