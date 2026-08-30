package com.example.noubasketalzira.core.auth

import com.example.noubasketalzira.core.domain.model.User
import kotlinx.coroutines.flow.StateFlow

data class ActiveTeamState(
    val teamId: String,
    val teamName: String,
    val role: String
)

data class SessionState(
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val userTeams: List<ActiveTeamState> = emptyList(),
    val activeTeam: ActiveTeamState? = null
)

interface ISessionManager {
    val sessionState: StateFlow<SessionState>
    fun setActiveTeam(teamId: String)
    suspend fun refreshSession()
    suspend fun logout()
}
