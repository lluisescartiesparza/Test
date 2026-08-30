package com.example.noubasketalzira.feature.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.feature.events.domain.model.Attendance
import com.example.noubasketalzira.feature.events.domain.model.AttendanceStatus
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailState(
    val canManage: Boolean = false,
    val canSummonAll: Boolean = false,
    val summonedPlayers: List<Attendance> = emptyList(),
    val unsummonedPlayers: List<Attendance> = emptyList()
)

class EventDetailViewModel(
    private val eventId: String,
    private val repository: IEventRepository,
    private val sessionManager: ISessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailState())
    val uiState: StateFlow<EventDetailState> = _uiState

    init {
        val session = sessionManager.sessionState.value
        val globalRole = session.user?.role
        val teamRole = session.activeTeam?.role
        val canManage = globalRole == UserRole.GERENCIA || 
                        globalRole == UserRole.DIRECTOR_DEPORTIVO || 
                        globalRole == UserRole.SUPERADMIN || 
                        teamRole == "ENTRENADOR"
        _uiState.update { it.copy(canManage = canManage) }

        viewModelScope.launch {
            repository.observeAttendance(eventId).collect { allAttendances ->
                val summoned = allAttendances.filter { it.status != AttendanceStatus.NO_CONVOCADO }
                val unsummoned = allAttendances.filter { it.status == AttendanceStatus.NO_CONVOCADO }
                
                val canSummonAll = summoned.isEmpty() && unsummoned.isNotEmpty()
                
                _uiState.update {
                    it.copy(
                        summonedPlayers = summoned,
                        unsummonedPlayers = unsummoned,
                        canSummonAll = canSummonAll
                    )
                }
            }
        }
    }

    fun summonAll() {
        viewModelScope.launch {
            repository.markAllAs(eventId, AttendanceStatus.NO_ASISTENCIA)
        }
    }

    fun summonPlayer(attendance: Attendance) {
        viewModelScope.launch {
            repository.updateAttendanceStatus(eventId, attendance.userId, AttendanceStatus.NO_ASISTENCIA)
        }
    }

    fun removePlayer(attendance: Attendance) {
        viewModelScope.launch {
            repository.updateAttendanceStatus(eventId, attendance.userId, AttendanceStatus.NO_CONVOCADO)
        }
    }

    fun rotateStatus(attendance: Attendance) {
        val nextStatus = when (attendance.status) {
            AttendanceStatus.NO_ASISTENCIA -> AttendanceStatus.ASISTENCIA
            AttendanceStatus.ASISTENCIA -> AttendanceStatus.RETRASO
            AttendanceStatus.RETRASO -> AttendanceStatus.JUSTIFICADA
            AttendanceStatus.JUSTIFICADA -> AttendanceStatus.NO_ASISTENCIA
            else -> AttendanceStatus.NO_ASISTENCIA
        }
        viewModelScope.launch {
            repository.updateAttendanceStatus(eventId, attendance.userId, nextStatus)
        }
    }
}
