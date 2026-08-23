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

class EventRepositoryImpl(
    private val eventDao: EventDao,
    private val attendanceDao: AttendanceDao,
    private val teamMemberDao: TeamMemberDao,
    private val userDao: UserDao,
    private val context: Context
) : IEventRepository {

    override fun observeEvents(teamId: String): Flow<List<Event>> {
        return eventDao.observeEventsByTeam(teamId).map { entities -> 
            entities.map { it.toDomain() } 
        }
    }

    override fun observeAttendance(eventId: String): Flow<List<Attendance>> {
        // This requires joining with users. Since Flow map runs in coroutine, we can fetch user names.
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
        }
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
            // 1. Insert Event
            eventDao.insertEvent(newEvent)

            // 2. Fetch all JUGADORES in this team
            val jugadores = teamMemberDao.getMembersByTeamIdAndRole(teamId, "JUGADOR")

            // 3. Insert Attendance records
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
        
        // 4. Enqueue Sync
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
        enqueueAttendanceSync(eventId, null) // Null means sync all for event, or we can handle it specifically
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
}
