package com.example.noubasketalzira.feature.events.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
import com.example.noubasketalzira.feature.events.domain.model.EventType
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

import kotlinx.coroutines.flow.flowOn

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class EventRepositoryImpl(
    private val eventDao: EventDao,
    private val attendanceDao: AttendanceDao,
    private val teamMemberDao: TeamMemberDao,
    private val userDao: UserDao,
    private val context: Context,
    private val supabase: SupabaseClient
) : IEventRepository {

    override fun observeEvents(teamId: String): Flow<List<Event>> {
        return eventDao.observeEventsByTeam(teamId).map { entities -> 
            entities.map { it.toDomain() } 
        }.flowOn(Dispatchers.IO)
    }

    override fun observeAttendance(eventId: String): Flow<List<Attendance>> {
        return attendanceDao.observeAttendanceByEvent(eventId).map { entities ->
            entities.map { entity ->
                val user = userDao.getUserById(entity.userId)
                Attendance(
                    eventId = entity.eventId,
                    userId = entity.userId,
                    userName = user?.fullName ?: "Unknown",
                    status = AttendanceStatus.valueOf(entity.status)
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createEvent(
        teamId: String,
        type: EventType,
        date: Long,
        description: String?
    ) {
        val newEventId = UUID.randomUUID().toString()
        val newEvent = EventEntity(
            id = newEventId,
            teamId = teamId,
            type = type,
            date = date,
            description = description,
            createdAt = System.currentTimeMillis()
        )

        withContext(Dispatchers.IO) {
            eventDao.insertEvent(newEvent)
            val jugadores = teamMemberDao.getMembersByTeamIdAndRole(teamId, "JUGADOR")
            val attendances = jugadores.map {
                AttendanceEntity(
                    eventId = newEventId,
                    userId = it.userId,
                    status = AttendanceStatus.NO_CONVOCADO.name,
                    createdAt = System.currentTimeMillis()
                )
            }
            if (attendances.isNotEmpty()) {
                attendanceDao.insertAttendances(attendances)
            }
        }
        
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder()
            .putString("action", "INSERT_EVENT")
            .putString("eventId", newEventId)
            .build()
            
        val request = OneTimeWorkRequestBuilder<com.example.noubasketalzira.feature.events.data.worker.EventSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }

    override suspend fun deleteEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(eventId)
        }
        
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder()
            .putString("action", "DELETE_EVENT")
            .putString("eventId", eventId)
            .build()
            
        val request = OneTimeWorkRequestBuilder<com.example.noubasketalzira.feature.events.data.worker.EventSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }

    override suspend fun updateAttendanceStatus(
        eventId: String,
        userId: String,
        status: AttendanceStatus
    ) {
        withContext(Dispatchers.IO) {
            attendanceDao.updateAttendanceStatus(eventId, userId, status.name)
        }
        enqueueAttendanceSync(eventId, userId)
    }

    override suspend fun markAllAs(eventId: String, status: AttendanceStatus) {
        withContext(Dispatchers.IO) {
            attendanceDao.updateAllAttendanceStatus(eventId, status.name)
        }
        enqueueAttendanceSync(eventId, null)
    }
    
    private fun enqueueAttendanceSync(eventId: String, userId: String?) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder()
            .putString("eventId", eventId)
        if (userId != null) {
            data.putString("userId", userId)
        }
        
        val request = OneTimeWorkRequestBuilder<com.example.noubasketalzira.feature.events.data.worker.AttendanceSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }

    override suspend fun syncEvents(teamId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Fetch events
                val remoteEvents = supabase.postgrest["events"]
                    .select { filter { eq("team_id", teamId) } }
                    .decodeList<com.example.noubasketalzira.feature.events.data.worker.EventInsertDto>()
                
                remoteEvents.forEach { dto ->
                    eventDao.insertEvent(EventEntity(
                        id = dto.id,
                        teamId = dto.team_id,
                        type = EventType.valueOf(dto.type),
                        date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()).parse(dto.date)?.time ?: System.currentTimeMillis(),
                        description = dto.description,
                        createdAt = System.currentTimeMillis()
                    ))
                    
                    // Fetch attendance for this event
                    val remoteAtt = supabase.postgrest["attendance"]
                        .select { filter { eq("event_id", dto.id) } }
                        .decodeList<com.example.noubasketalzira.feature.events.data.worker.AttendanceInsertDto>()
                    
                    val attEntities = remoteAtt.map { att ->
                        AttendanceEntity(
                            eventId = att.event_id,
                            userId = att.user_id,
                            status = att.status,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                    if (attEntities.isNotEmpty()) {
                        attendanceDao.insertAttendances(attEntities)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EventRepositoryImpl", "Sync events failed", e)
            }
        }
    }
}
