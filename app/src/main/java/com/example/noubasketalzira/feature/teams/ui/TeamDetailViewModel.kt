package com.example.noubasketalzira.feature.teams.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.feature.teams.domain.model.TeamRole
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository
import com.example.noubasketalzira.feature.users.data.repository.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MemberWithUser(
    val user: User,
    val role: TeamRole
)

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TeamDetailViewModel(
    initialTeamId: String,
    private val teamRepository: ITeamRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _teamId = MutableStateFlow(initialTeamId)

    fun setTeamId(newTeamId: String) {
        _teamId.value = newTeamId
    }

    val members: StateFlow<List<MemberWithUser>> = _teamId
        .flatMapLatest { teamId ->
            combine(
                teamRepository.observeTeamMembers(teamId),
                userRepository.observeUsers()
            ) { teamMembers, allUsers ->
                teamMembers.mapNotNull { tm ->
                    val user = allUsers.find { it.id == tm.userId }
                    if (user != null) {
                        MemberWithUser(user, tm.role)
                    } else null
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val allUsers: StateFlow<List<User>> = userRepository.observeUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun assignMember(userId: String, role: TeamRole) {
        viewModelScope.launch {
            teamRepository.assignMember(_teamId.value, userId, role)
        }
    }
    
    fun updateMemberRole(userId: String, role: TeamRole) {
        viewModelScope.launch {
            teamRepository.assignMember(_teamId.value, userId, role) // UPSERT in Room handles it
        }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch {
            teamRepository.removeMember(_teamId.value, userId)
        }
    }
}
