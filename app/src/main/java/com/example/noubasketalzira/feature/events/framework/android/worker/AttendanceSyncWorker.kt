package com.example.noubasketalzira.feature.events.framework.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.jan.supabase.SupabaseClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import android.util.Log
import com.example.noubasketalzira.core.data.local.dao.AttendanceDao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

import com.example.noubasketalzira.feature.events.data.source.remote.dto.AttendanceInsertDto
import com.example.noubasketalzira.feature.events.data.source.remote.dto.AttendanceUpdateDto

class AttendanceSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val supabase: SupabaseClient by inject()
    private val attendanceDao: AttendanceDao by inject()

    override suspend fun doWork(): Result {
        val eventId = inputData.getString("eventId") ?: return Result.failure()
        val userId = inputData.getString("userId") // Null means sync all for event

        try {
            val attendancesFlow = attendanceDao.observeAttendanceByEvent(eventId)
            val attendances = attendancesFlow.first()

            if (userId != null) {
                // Sync specific user
                val attendance = attendances.find { it.userId == userId } ?: return Result.failure()
                supabase.postgrest["attendance"].upsert(
                    AttendanceInsertDto(event_id = eventId, user_id = userId, status = attendance.status)
                )
            } else {
                // Sync all users for event
                val dtos = attendances.map { 
                    AttendanceInsertDto(event_id = eventId, user_id = it.userId, status = it.status)
                }
                if (dtos.isNotEmpty()) {
                    supabase.postgrest["attendance"].upsert(dtos)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("AttendanceSyncWorker", "Sync failed", e)
            return Result.retry()
        }
    }
}
