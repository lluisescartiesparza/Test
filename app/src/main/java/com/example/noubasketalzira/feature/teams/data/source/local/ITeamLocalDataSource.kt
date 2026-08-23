package com.example.noubasketalzira.feature.teams.data.source.local

import com.example.noubasketalzira.feature.teams.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface ITeamLocalDataSource {
    fun observeTeams(): Flow<List<Team>>
    suspend fun insertTeam(team: Team)
    suspend fun deleteTeam(teamId: String)
    suspend fun insertTeamMember(teamId: String, userId: String, role: String)
    suspend fun insertUser(id: String, email: String, fullName: String, role: String)
}
