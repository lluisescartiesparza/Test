package com.example.noubasketalzira.feature.teams.data.repository

import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.entity.TeamEntity
import com.example.noubasketalzira.core.data.local.entity.TeamMemberEntity
import com.example.noubasketalzira.core.data.local.entity.toDomain
import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class TeamRepositoryImpl(
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao
) : ITeamRepository {

    override fun observeTeams(): Flow<List<Team>> {
        return teamDao.observeAllTeams().map { entities -> 
            entities.map { it.toDomain() } 
        }
    }

    override suspend fun createTeam(name: String, category: String) {
        val newTeam = TeamEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            createdAt = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) {
            teamDao.insertTeam(newTeam)
        }
        // TODO: Sync to remote in background
    }

    override suspend fun deleteTeam(teamId: String) {
        withContext(Dispatchers.IO) {
            teamDao.deleteTeam(teamId)
        }
        // TODO: Sync to remote in background
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
        // TODO: Fetch from Supabase and insert into local Room DAOs
    }
}
