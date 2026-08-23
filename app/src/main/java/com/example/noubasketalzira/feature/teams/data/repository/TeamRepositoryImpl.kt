package com.example.noubasketalzira.feature.teams.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.entity.TeamEntity
import com.example.noubasketalzira.core.data.local.entity.TeamMemberEntity
import com.example.noubasketalzira.core.data.local.entity.toDomain
import com.example.noubasketalzira.feature.teams.data.worker.TeamSyncWorker
import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import com.example.noubasketalzira.core.data.remote.dto.TeamDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class TeamRepositoryImpl(
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao,
    private val supabase: SupabaseClient,
    private val context: Context
) : ITeamRepository {

    override fun observeTeams(): Flow<List<Team>> {
        return teamDao.observeAllTeams().map { entities -> 
            entities.map { it.toDomain() } 
        }
    }

    override suspend fun createTeam(name: String, category: String) {
        val newId = UUID.randomUUID().toString()
        val newTeam = TeamEntity(
            id = newId,
            name = name,
            category = category,
            createdAt = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) {
            teamDao.insertTeam(newTeam)
        }
        
        // Encolar trabajo offline-first
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val data = Data.Builder()
            .putString(TeamSyncWorker.KEY_ACTION, TeamSyncWorker.ACTION_INSERT)
            .putString(TeamSyncWorker.KEY_TEAM_ID, newId)
            .putString(TeamSyncWorker.KEY_TEAM_NAME, name)
            .putString(TeamSyncWorker.KEY_TEAM_CATEGORY, category)
            .build()
            
        val request = OneTimeWorkRequestBuilder<TeamSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }

    override suspend fun deleteTeam(teamId: String) {
        withContext(Dispatchers.IO) {
            teamDao.deleteTeam(teamId)
        }
        
        // Encolar trabajo offline-first
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val data = Data.Builder()
            .putString(TeamSyncWorker.KEY_ACTION, TeamSyncWorker.ACTION_DELETE)
            .putString(TeamSyncWorker.KEY_TEAM_ID, teamId)
            .build()
            
        val request = OneTimeWorkRequestBuilder<TeamSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }

    override suspend fun assignMember(teamId: String, userId: String, role: com.example.noubasketalzira.feature.teams.domain.model.TeamRole) {
        val newMember = TeamMemberEntity(
            teamId = teamId,
            userId = userId,
            role = role.name,
            createdAt = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) {
            teamMemberDao.insertTeamMember(newMember)
        }
        // TODO: Sync to remote in background
    }

    override suspend fun syncTeams() {
        withContext(Dispatchers.IO) {
            try {
                val remoteTeams = supabase.postgrest["teams"].select().decodeList<TeamDto>()
                remoteTeams.forEach { dto ->
                    teamDao.insertTeam(
                        TeamEntity(
                            id = dto.id,
                            name = dto.name,
                            category = dto.category,
                            createdAt = System.currentTimeMillis() // Or use dto.createdAt if parsed
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("SupabaseSync", "Error en red: ${e.message}", e)
            }
        }
    }
}
