package com.example.noubasketalzira.feature.teams.data.repository

import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.entity.TeamEntity
import com.example.noubasketalzira.core.data.local.entity.TeamMemberEntity
import com.example.noubasketalzira.core.data.local.entity.toDomain
import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import com.example.noubasketalzira.core.data.remote.dto.TeamDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class TeamRepositoryImpl(
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao,
    private val supabase: SupabaseClient
) : ITeamRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        
        // Background remote sync
        scope.launch {
            try {
                val dto = TeamDto(
                    id = newId,
                    name = name,
                    category = category
                )
                supabase.postgrest["teams"].insert(dto)
            } catch (e: Exception) {
                // Ignore remote errors for now to keep SSOT offline-first functioning
            }
        }
    }

    override suspend fun deleteTeam(teamId: String) {
        withContext(Dispatchers.IO) {
            teamDao.deleteTeam(teamId)
        }
        
        // Background remote sync
        scope.launch {
            try {
                supabase.postgrest["teams"].delete {
                    filter { eq("id", teamId) }
                }
            } catch (e: Exception) {
                // Ignore remote errors
            }
        }
    }

    override suspend fun assignMember(teamId: String, userId: String, role: String) {
        val newMember = TeamMemberEntity(
            teamId = teamId,
            userId = userId,
            role = role,
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
                // Ignore remote errors
            }
        }
    }
}
