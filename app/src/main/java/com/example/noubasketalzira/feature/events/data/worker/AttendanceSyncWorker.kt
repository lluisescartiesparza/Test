package com.example.noubasketalzira.feature.events.data.worker

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

@Serializable
data class AttendanceUpdateDto(val status: String)

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
                supabase.postgrest["attendance"].update(
                    AttendanceUpdateDto(status = attendance.status)
                ) {
                    filter {
                        eq("event_id", eventId)
                        eq("user_id", userId)
                    }
                }
            } else {
                // Sync all users for event
                for (attendance in attendances) {
                    supabase.postgrest["attendance"].update(
                        AttendanceUpdateDto(status = attendance.status)
                    ) {
                        filter {
                            eq("event_id", eventId)
                            eq("user_id", attendance.userId)
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("AttendanceSyncWorker", "Sync failed", e)
            return Result.retry()
        }
    }
}
