package com.example.noubasketalzira.core.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.dao.UserDao
import com.example.noubasketalzira.core.data.local.entity.toDomain
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionManagerImpl(
    private val context: Context,
    private val supabaseClient: SupabaseClient,
    private val userDao: UserDao,
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao
) : ISessionManager {

    private val prefs: SharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _sessionState = MutableStateFlow(SessionState())
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        scope.launch {
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val userId = status.session.user?.id
                        if (userId != null) {
                            loadUserSession(userId)
                        } else {
                            clearSession()
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        clearSession()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadUserSession(userId: String) {
        val userEntity = userDao.getUserById(userId)
        if (userEntity != null) {
            val memberships = teamMemberDao.getMembersByUserId(userId)
            val allTeams = teamDao.getAllTeams()
            
            val userTeams = memberships.mapNotNull { member ->
                val team = allTeams.find { it.id == member.teamId }
                if (team != null) {
                    ActiveTeamState(teamId = team.id, teamName = team.name, role = member.role)
                } else null
            }
            
            val lastActiveTeamId = prefs.getString("active_team_id", null)
            var activeTeam = userTeams.find { it.teamId == lastActiveTeamId }
            
            if (activeTeam == null && userTeams.isNotEmpty()) {
                activeTeam = userTeams.first()
                prefs.edit().putString("active_team_id", activeTeam.teamId).apply()
            }
            
            _sessionState.update { 
                it.copy(
                    isLoggedIn = true,
                    user = userEntity.toDomain(),
                    userTeams = userTeams,
                    activeTeam = activeTeam
                )
            }
        } else {
            // User authenticated but not in local DB yet. 
            // In a real app we'd fetch from remote here or wait for sync.
            _sessionState.update { it.copy(isLoggedIn = true) }
        }
    }

    private fun clearSession() {
        _sessionState.update { SessionState() }
    }

    override fun setActiveTeam(teamId: String) {
        val teams = _sessionState.value.userTeams
        val newActive = teams.find { it.teamId == teamId }
        if (newActive != null) {
            prefs.edit().putString("active_team_id", teamId).apply()
            _sessionState.update { it.copy(activeTeam = newActive) }
        }
    }

    override suspend fun refreshSession() {
        val user = supabaseClient.auth.currentUserOrNull()
        if (user != null) {
            withContext(Dispatchers.IO) {
                loadUserSession(user.id)
            }
        }
    }

    override suspend fun logout() {
        supabaseClient.auth.signOut()
    }
}
