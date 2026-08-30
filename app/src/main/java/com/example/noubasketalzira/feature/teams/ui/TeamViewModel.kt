package com.example.noubasketalzira.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.core.domain.util.IIdGenerator
import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: ITeamRepository,
    private val sessionManager: ISessionManager,
    private val idGenerator: IIdGenerator
) : ViewModel() {

    val teams: StateFlow<List<Team>> = teamRepository.observeTeams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            teamRepository.syncTeams()
        }
    }

    private fun hasManagementRole(): Boolean {
        val user = sessionManager.currentUser.value
        return user?.role == UserRole.GERENCIA || user?.role == UserRole.DIRECTOR_DEPORTIVO
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

    fun assignMockMember(teamId: String, role: com.example.noubasketalzira.feature.teams.domain.model.TeamRole = com.example.noubasketalzira.feature.teams.domain.model.TeamRole.ENTRENADOR) {
        viewModelScope.launch {
            val dummyUserId = idGenerator.generateUniqueId()
            teamRepository.assignMember(teamId, dummyUserId, role)
        }
    }
}
