package com.example.noubasketalzira.feature.events.data.repository

import com.example.noubasketalzira.core.domain.scheduler.ISyncScheduler
import com.example.noubasketalzira.core.domain.util.IDateFormatter
import com.example.noubasketalzira.core.domain.util.IIdGenerator
import com.example.noubasketalzira.feature.events.data.source.local.IEventLocalDataSource
import com.example.noubasketalzira.feature.events.data.source.remote.IEventRemoteDataSource
import com.example.noubasketalzira.feature.events.domain.model.Attendance
import com.example.noubasketalzira.feature.events.domain.model.AttendanceStatus
import com.example.noubasketalzira.feature.events.domain.model.Event
import com.example.noubasketalzira.feature.events.domain.model.EventType
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class EventRepositoryImpl(
    private val localDataSource: IEventLocalDataSource,
    private val remoteDataSource: IEventRemoteDataSource,
    private val syncScheduler: ISyncScheduler,
    private val idGenerator: IIdGenerator,
    private val dateFormatter: IDateFormatter
) : IEventRepository {

    override fun observeEvents(teamId: String): Flow<List<Event>> {
        return localDataSource.observeEvents(teamId).flowOn(Dispatchers.IO)
    }

    override fun observeAttendance(eventId: String): Flow<List<Attendance>> {
        return localDataSource.observeAttendance(eventId).flowOn(Dispatchers.IO)
    }

    override suspend fun createEvent(
        teamId: String,
        type: EventType,
        date: Long,
        description: String?
    ) {
        val newEventId = idGenerator.generateUniqueId()
        val newEvent = Event(
            id = newEventId,
            teamId = teamId,
            type = type,
            date = date,
            description = description
        )

        withContext(Dispatchers.IO) {
            val playerIds = localDataSource.getPlayersForTeam(teamId)
            if (playerIds.isEmpty()) {
                throw IllegalStateException("El equipo no tiene jugadores. No se puede crear un evento.")
            }

            localDataSource.insertEvent(newEvent)
            
            val attendances = playerIds.map { userId ->
                Attendance(
                    eventId = newEventId,
                    userId = userId,
                    userName = "", // Irrelevant for insert
                    status = AttendanceStatus.NO_CONVOCADO
                )
            }
            if (attendances.isNotEmpty()) {
                localDataSource.insertAttendances(attendances)
            }
        }
        
        syncScheduler.scheduleEventSync("insert", newEventId)
    }

    override suspend fun deleteEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            localDataSource.deleteEvent(eventId)
        }
        syncScheduler.scheduleEventSync("delete", eventId)
    }

    override suspend fun updateAttendanceStatus(
        eventId: String,
        userId: String,
        status: AttendanceStatus
    ) {
        withContext(Dispatchers.IO) {
            localDataSource.updateAttendanceStatus(eventId, userId, status.name)
        }
        syncScheduler.scheduleAttendanceSync(eventId, userId)
    }

    override suspend fun markAllAs(eventId: String, status: AttendanceStatus) {
        withContext(Dispatchers.IO) {
            val currentAttendances = localDataSource.observeAttendance(eventId).first()
            val attendancesToUpsert = currentAttendances.map { att ->
                Attendance(
                    eventId = eventId,
                    userId = att.userId,
                    userName = att.userName,
                    status = status
                )
            }
            if (attendancesToUpsert.isNotEmpty()) {
                localDataSource.insertAttendances(attendancesToUpsert)
            }
        }
        syncScheduler.scheduleAttendanceSync(eventId, null)
    }

    override suspend fun syncEvents(teamId: String) {
        withContext(Dispatchers.IO) {
            try {
                val remoteEvents = remoteDataSource.fetchEvents(teamId)
                
                remoteEvents.forEach { dto ->
                    val parsedDate = dateFormatter.parseIso8601ToTimestamp(dto.date)

                    localDataSource.insertEvent(Event(
                        id = dto.id,
                        teamId = dto.team_id,
                        type = EventType.valueOf(dto.type),
                        date = parsedDate,
                        description = dto.description
                    ))
                    
                    val remoteAtt = remoteDataSource.fetchAttendance(dto.id)
                    val attendances = remoteAtt.map { att ->
                        Attendance(
                            eventId = att.event_id,
                            userId = att.user_id,
                            userName = "",
                            status = AttendanceStatus.valueOf(att.status)
                        )
                    }
                    if (attendances.isNotEmpty()) {
                        localDataSource.insertAttendances(attendances)
                    }
                }
            } catch (e: Exception) {
                // Silently ignore sync errors for offline first
            }
        }
    }

    override suspend fun hasPlayers(teamId: String): Boolean {
        return withContext(Dispatchers.IO) {
            localDataSource.getPlayersForTeam(teamId).isNotEmpty()
        }
    }
}
