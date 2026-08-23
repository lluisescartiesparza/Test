package com.example.noubasketalzira.feature.teams.domain.repository

import com.example.noubasketalzira.feature.teams.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface ITeamRepository {
    fun observeTeams(): Flow<List<Team>>
    suspend fun createTeam(name: String, category: String)
    suspend fun deleteTeam(teamId: String)
    suspend fun assignMember(teamId: String, userId: String, role: com.example.noubasketalzira.feature.teams.domain.model.TeamRole)
    
    // Remote sync
    suspend fun syncTeams()
}
