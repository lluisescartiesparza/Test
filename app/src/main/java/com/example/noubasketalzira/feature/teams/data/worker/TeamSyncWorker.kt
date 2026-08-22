package com.example.noubasketalzira.feature.teams.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.noubasketalzira.core.data.remote.dto.TeamDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TeamSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val supabase: SupabaseClient by inject()

    override suspend fun doWork(): Result {
        val action = inputData.getString(KEY_ACTION) ?: return Result.failure()
        val teamId = inputData.getString(KEY_TEAM_ID) ?: return Result.failure()

        return try {
            when (action) {
                ACTION_INSERT -> {
                    val name = inputData.getString(KEY_TEAM_NAME) ?: return Result.failure()
                    val category = inputData.getString(KEY_TEAM_CATEGORY)
                    val dto = TeamDto(id = teamId, name = name, category = category)
                    supabase.postgrest["teams"].insert(dto)
                }
                ACTION_DELETE -> {
                    supabase.postgrest["teams"].delete {
                        filter { eq("id", teamId) }
                    }
                }
                else -> return Result.failure()
            }
            Log.d("TeamSyncWorker", "Operación $action exitosa para $teamId")
            Result.success()
        } catch (e: Exception) {
            Log.e("TeamSyncWorker", "Fallo al sincronizar $action para $teamId. Reintentando...", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_ACTION = "action"
        const val ACTION_INSERT = "insert"
        const val ACTION_DELETE = "delete"
        
        const val KEY_TEAM_ID = "team_id"
        const val KEY_TEAM_NAME = "team_name"
        const val KEY_TEAM_CATEGORY = "team_category"
    }
}
