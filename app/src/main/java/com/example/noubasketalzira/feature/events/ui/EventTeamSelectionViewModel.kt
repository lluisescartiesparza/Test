package com.example.noubasketalzira.feature.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao
import com.example.noubasketalzira.core.data.local.entity.toDomain
import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventTeamSelectionState(
    val isLoading: Boolean = true,
    val teams: List<Team> = emptyList()
)

class EventTeamSelectionViewModel(
    private val sessionManager: ISessionManager,
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao,
    private val teamRepository: ITeamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventTeamSelectionState())
    val uiState: StateFlow<EventTeamSelectionState> = _uiState

    init {
        loadTeams()
        viewModelScope.launch {
            teamRepository.syncTeams()
        }
    }

    private fun loadTeams() {
        viewModelScope.launch {
            val user = sessionManager.currentUser.value ?: return@launch
            
            // Depending on role, fetch all or specific teams
            if (user.role == UserRole.GERENCIA || user.role == UserRole.DIRECTOR_DEPORTIVO) {
                // Fetch all
                teamDao.observeAllTeams().collect { entities ->
                    _uiState.update { it.copy(isLoading = false, teams = entities.map { it.toDomain() }) }
                }
            } else {
                // Fetch assigned
                val members = teamMemberDao.getMembersByUserId(user.id)
                val teamIds = members.map { it.teamId }
                
                // Realistically we need a IN query, but for simplicity we fetch all and filter
                teamDao.observeAllTeams().collect { entities ->
                    val filtered = entities.filter { it.id in teamIds }.map { it.toDomain() }
                    _uiState.update { it.copy(isLoading = false, teams = filtered) }
                }
            }
        }
    }
}
