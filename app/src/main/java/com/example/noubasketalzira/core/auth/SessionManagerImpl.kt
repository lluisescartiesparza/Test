package com.example.noubasketalzira.core.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.dao.UserDao
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

import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository

class SessionManagerImpl(
    private val context: Context,
    private val supabaseClient: SupabaseClient,
    private val userDao: UserDao,
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao,
    private val teamRepository: ITeamRepository
) : ISessionManager {


    private val _sessionState = MutableStateFlow(SessionState())
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val prefs: SharedPreferences = context.getSharedPreferences("noubasket_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.Main)

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

    private fun loadUserSession(authUserId: String) {
        scope.launch(Dispatchers.IO) {
            val email = supabaseClient.auth.currentUserOrNull()?.email
            android.util.Log.e("NouBasketAuth", "loadUserSession started for AuthId: $authUserId, Email: $email")
            
            try {
                android.util.Log.e("NouBasketAuth", "Forcing full sync (users, teams, members)...")
                teamRepository.syncTeams()
            } catch (e: Exception) {
                android.util.Log.e("NouBasketAuth", "Sync failed on login: ${e.message}")
            }
            
            var userEntity = userDao.getUserById(authUserId)
            if (userEntity == null && email != null) {
                userEntity = userDao.getUserByEmail(email)
                android.util.Log.e("NouBasketAuth", "Fallback to getUserByEmail found: $userEntity")
            }
            
            android.util.Log.e("NouBasketAuth", "userEntity after sync/fallback: $userEntity")
            
            if (userEntity != null) {
                val dbUserId = userEntity.id
                android.util.Log.e("NouBasketAuth", "User entity found with role: ${userEntity.role}")
                val memberships = teamMemberDao.getMembersByUserId(dbUserId)
                val allTeams = teamDao.getAllTeams()
                
                val userTeams = if (userEntity.role == com.example.noubasketalzira.core.domain.model.UserRole.SUPERADMIN || 
                                    userEntity.role == com.example.noubasketalzira.core.domain.model.UserRole.GERENCIA ||
                                    userEntity.role == com.example.noubasketalzira.core.domain.model.UserRole.DIRECTOR_DEPORTIVO) {
                    android.util.Log.e("NouBasketAuth", "Granting global access to ${allTeams.size} teams")
                    allTeams.map { team ->
                        ActiveTeamState(
                            teamId = team.id, 
                            teamName = team.name, 
                            role = "ENTRENADOR"
                        )
                    }
                } else {
                    android.util.Log.e("NouBasketAuth", "Found ${memberships.size} direct memberships for user $dbUserId")
                    memberships.mapNotNull { member ->
                        allTeams.find { it.id == member.teamId }?.let { team ->
                            ActiveTeamState(teamId = team.id, teamName = team.name, role = member.role)
                        }
                    }
                }
                
                val lastActiveTeamId = prefs.getString("active_team_id", null)
                var activeTeam = userTeams.find { it.teamId == lastActiveTeamId }
                
                if (activeTeam == null && userTeams.size == 1) {
                    activeTeam = userTeams.first()
                    prefs.edit().putString("active_team_id", activeTeam.teamId).apply()
                }
                
                _sessionState.update { 
                    it.copy(
                        isLoggedIn = true,
                        user = User(
                            id = dbUserId, 
                            email = userEntity.email, 
                            fullName = userEntity.fullName, 
                            role = userEntity.role
                        ),
                        userTeams = userTeams,
                        activeTeam = activeTeam
                    ) 
                }
            } else {
                android.util.Log.e("NouBasketAuth", "userEntity still null after sync")
                clearSession()
            }
        }
    }

    override fun setActiveTeam(teamId: String) {
        val team = _sessionState.value.userTeams.find { it.teamId == teamId }
        if (team != null) {
            prefs.edit().putString("active_team_id", teamId).apply()
            _sessionState.update { it.copy(activeTeam = team) }
        }
    }

    private fun clearSession() {
        prefs.edit().remove("active_team_id").apply()
        _sessionState.update { SessionState() }
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
