package com.example.noubasketalzira.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.feature.teams.domain.model.Team
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TeamViewModel(
    private val teamRepository: ITeamRepository,
    private val sessionManager: ISessionManager
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
        // TODO: En el futuro descomentar esto para bloquear la acción
        /*
        if (!hasManagementRole()) {
            // Mostrar error o ignorar
            return
        }
        */
        
        viewModelScope.launch {
            teamRepository.createTeam(name, category)
        }
    }

    fun deleteTeam(team: Team) {
        // TODO: En el futuro descomentar esto para bloquear la acción
        /*
        if (!hasManagementRole()) {
            return
        }
        */
        
        viewModelScope.launch {
            teamRepository.deleteTeam(team.id)
        }
    }

    fun assignMockMember(teamId: String, role: String = "ENTRENADOR") {
        // TODO: En el futuro descomentar esto para bloquear la acción
        /*
        if (!hasManagementRole()) {
            return
        }
        */
        
        viewModelScope.launch {
            // Generamos un usuario UUID aleatorio para la prueba
            val dummyUserId = UUID.randomUUID().toString()
            teamRepository.assignMember(teamId, dummyUserId, role)
        }
    }
}
