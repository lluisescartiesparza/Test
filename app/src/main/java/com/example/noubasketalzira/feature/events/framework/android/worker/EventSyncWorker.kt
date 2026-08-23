package com.example.noubasketalzira.feature.events.framework.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.jan.supabase.SupabaseClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import android.util.Log
import com.example.noubasketalzira.core.data.local.dao.AttendanceDao
import com.example.noubasketalzira.core.data.local.dao.EventDao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

import com.example.noubasketalzira.feature.events.data.source.remote.dto.AttendanceInsertDto
import com.example.noubasketalzira.feature.events.data.source.remote.dto.EventInsertDto

class EventSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val supabase: SupabaseClient by inject()
    private val eventDao: EventDao by inject()
    private val attendanceDao: AttendanceDao by inject()

    override suspend fun doWork(): Result {
        val eventId = inputData.getString("eventId") ?: return Result.failure()
        val action = inputData.getString("action") ?: "INSERT_EVENT"

        try {
            if (action == "DELETE_EVENT") {
                supabase.postgrest["events"].delete {
                    filter { eq("id", eventId) }
                }
                return Result.success()
            }

            // For INSERT_EVENT
            val event = eventDao.getEventById(eventId) ?: return Result.failure()
            val attendancesFlow = attendanceDao.observeAllTeamAttendances(eventId)
            val attendances = attendancesFlow.first()

            val eventDto = EventInsertDto(
                id = event.id,
                team_id = event.teamId,
                type = event.type.name,
                date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()).format(java.util.Date(event.date)),
                description = event.description
            )

            supabase.postgrest["events"].upsert(eventDto)
            
            val attDtos = attendances.map { 
                AttendanceInsertDto(event_id = it.eventId, user_id = it.userId, status = it.status) 
            }
            if (attDtos.isNotEmpty()) {
                supabase.postgrest["attendance"].upsert(attDtos)
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("EventSyncWorker", "Sync failed", e)
            return Result.retry()
        }
    }
}
