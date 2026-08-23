package com.example.noubasketalzira.feature.teams.data.source.local

import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.entity.TeamEntity
import com.example.noubasketalzira.core.data.local.entity.toDomain
import com.example.noubasketalzira.feature.teams.domain.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.dao.UserDao

class TeamLocalDataSourceImpl(
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao,
    private val userDao: UserDao
) : ITeamLocalDataSource {
    override fun observeTeams(): Flow<List<Team>> {
        return teamDao.observeAllTeams().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insertTeam(team: Team) {
        teamDao.insertTeam(TeamEntity(
            id = team.id,
            name = team.name,
            category = team.category,
            createdAt = System.currentTimeMillis() // Or pass from domain
        ))
    }

    override suspend fun deleteTeam(teamId: String) {
        teamDao.deleteTeam(teamId)
    }

    override suspend fun insertTeamMember(teamId: String, userId: String, role: String) {
        teamMemberDao.insertTeamMember(
            com.example.noubasketalzira.core.data.local.entity.TeamMemberEntity(
                teamId = teamId,
                userId = userId,
                role = role,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun insertUser(id: String, email: String, fullName: String, role: String) {
        userDao.insertUser(
            com.example.noubasketalzira.core.data.local.entity.UserEntity(
                id = id,
                email = email,
                fullName = fullName,
                role = com.example.noubasketalzira.core.domain.model.UserRole.valueOf(role),
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
