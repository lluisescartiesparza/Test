package com.example.noubasketalzira.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.core.domain.util.IIdGenerator
import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.model.TeamRole
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import com.example.noubasketalzira.feature.users.data.repository.IUserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: ITeamRepository,
    private val userRepository: IUserRepository,
    private val sessionManager: ISessionManager,
    private val idGenerator: IIdGenerator
) : ViewModel() {

    val teams: StateFlow<List<Team>> = teamRepository.observeTeams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val users: StateFlow<List<User>> = userRepository.observeUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val canManageTeams: StateFlow<Boolean> = sessionManager.sessionState
        .map { state ->
            state.user?.role == UserRole.GERENCIA || 
            state.user?.role == UserRole.DIRECTOR_DEPORTIVO || 
            state.user?.role == UserRole.SUPERADMIN
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        viewModelScope.launch {
            teamRepository.syncTeams()
            userRepository.syncUsers()
        }
    }

    fun createTeam(name: String, category: String) {
        viewModelScope.launch {
            teamRepository.createTeam(name, category)
        }
    }

    fun deleteTeam(team: Team) {
        viewModelScope.launch {
            teamRepository.deleteTeam(team.id)
        }
    }

    fun assignMember(teamId: String, userId: String, role: TeamRole) {
        viewModelScope.launch {
            teamRepository.assignMember(teamId, userId, role)
        }
    }
}
