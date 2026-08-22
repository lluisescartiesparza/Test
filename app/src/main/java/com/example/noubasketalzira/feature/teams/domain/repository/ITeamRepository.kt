package com.example.noubasketalzira.feature.teams.domain.repository

import com.example.noubasketalzira.core.data.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

interface ITeamRepository {
    fun observeTeams(): Flow<List<TeamEntity>>
    suspend fun createTeam(name: String, category: String)
    suspend fun deleteTeam(team: TeamEntity)
    suspend fun assignMember(teamId: String, userId: String, role: String)
    
    // Remote sync
    suspend fun syncTeams()
}
