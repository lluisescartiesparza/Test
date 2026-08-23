package com.example.noubasketalzira.feature.teams.data.repository

import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

import com.example.noubasketalzira.core.domain.scheduler.ISyncScheduler
import com.example.noubasketalzira.feature.teams.data.source.local.ITeamLocalDataSource
import com.example.noubasketalzira.feature.teams.data.source.remote.ITeamRemoteDataSource

class TeamRepositoryImpl(
    private val localDataSource: ITeamLocalDataSource,
    private val remoteDataSource: ITeamRemoteDataSource,
    private val syncScheduler: ISyncScheduler
) : ITeamRepository {

    override fun observeTeams(): Flow<List<Team>> {
        return localDataSource.observeTeams()
    }

    override suspend fun createTeam(name: String, category: String) {
        // Here we still use UUID but we can replace it later if needed for KMP (e.g. uuid library)
        val newId = UUID.randomUUID().toString()
        val newTeam = Team(
            id = newId,
            name = name,
            category = category
        )
        withContext(Dispatchers.IO) {
            localDataSource.insertTeam(newTeam)
        }
        
        syncScheduler.scheduleTeamSync("INSERT_EVENT", newId, name, category)
    }

    override suspend fun deleteTeam(teamId: String) {
        withContext(Dispatchers.IO) {
            localDataSource.deleteTeam(teamId)
        }
        syncScheduler.scheduleTeamSync("DELETE_EVENT", teamId)
    }

    override suspend fun assignMember(teamId: String, userId: String, role: com.example.noubasketalzira.feature.teams.domain.model.TeamRole) {
        withContext(Dispatchers.IO) {
            localDataSource.insertTeamMember(teamId, userId, role.name)
        }
    }

    override suspend fun syncTeams() {
        withContext(Dispatchers.IO) {
            try {
                val remoteTeams = remoteDataSource.fetchTeams()
                remoteTeams.forEach { dto ->
                    localDataSource.insertTeam(Team(
                        id = dto.id,
                        name = dto.name,
                        category = dto.category
                    ))
                }

                val remoteUsers = remoteDataSource.fetchUsers()
                remoteUsers.forEach { dto ->
                    localDataSource.insertUser(dto.id, dto.email, dto.fullName, dto.role)
                }

                val remoteMembers = remoteDataSource.fetchTeamMembers()
                remoteMembers.forEach { dto ->
                    localDataSource.insertTeamMember(dto.teamId, dto.userId, dto.role)
                }
            } catch (e: Exception) {
                // Should avoid android Log in domain/data if pure KMP, but let's just println or ignore for now to remove android.*
                println("Sync failed: ${e.message}")
            }
        }
    }
}
