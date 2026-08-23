package com.example.noubasketalzira.feature.events.data.repository

import com.example.noubasketalzira.core.domain.scheduler.ISyncScheduler
import com.example.noubasketalzira.feature.events.data.source.local.IEventLocalDataSource
import com.example.noubasketalzira.feature.events.data.source.remote.IEventRemoteDataSource
import com.example.noubasketalzira.feature.events.domain.model.Attendance
import com.example.noubasketalzira.feature.events.domain.model.AttendanceStatus
import com.example.noubasketalzira.feature.events.domain.model.Event
import com.example.noubasketalzira.feature.events.domain.model.EventType
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

// Wait, I should not use java.util.UUID or java.text.SimpleDateFormat here if I want strict KMP.
// But we still need UUID. We can just use a random string or keep UUID until a multiplatform library is added.
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class EventRepositoryImpl(
    private val localDataSource: IEventLocalDataSource,
    private val remoteDataSource: IEventRemoteDataSource,
    private val syncScheduler: ISyncScheduler
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
        val newEventId = UUID.randomUUID().toString()
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
        
        syncScheduler.scheduleEventSync("INSERT_EVENT", newEventId)
    }

    override suspend fun deleteEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            localDataSource.deleteEvent(eventId)
        }
        syncScheduler.scheduleEventSync("DELETE_EVENT", eventId)
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
            localDataSource.updateAllAttendanceStatus(eventId, status.name)
        }
        syncScheduler.scheduleAttendanceSync(eventId, null)
    }

    override suspend fun syncEvents(teamId: String) {
        withContext(Dispatchers.IO) {
            try {
                val remoteEvents = remoteDataSource.fetchEvents(teamId)
                
                remoteEvents.forEach { dto ->
                    // Parse date here for now. Ideal KMP solution would use kotlinx-datetime
                    val parsedDate = try {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).parse(dto.date)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

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
                println("Sync events failed: ${e.message}")
            }
        }
    }

    override suspend fun hasPlayers(teamId: String): Boolean {
        return withContext(Dispatchers.IO) {
            localDataSource.getPlayersForTeam(teamId).isNotEmpty()
        }
    }
}
