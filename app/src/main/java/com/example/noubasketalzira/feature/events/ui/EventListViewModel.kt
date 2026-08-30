package com.example.noubasketalzira.feature.events.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.domain.model.UserRole
import com.example.noubasketalzira.feature.events.domain.model.Event
import com.example.noubasketalzira.feature.events.domain.model.EventType
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

import com.example.noubasketalzira.feature.events.domain.usecase.GenerateEventsReportUseCase

data class EventListState(
    val events: List<Event> = emptyList(),
    val canManageEvents: Boolean = false,
    val hasPlayers: Boolean = false,
    val error: String? = null,
    val isGeneratingReport: Boolean = false,
    val showReportDialog: Boolean = false
)

class EventListViewModel(
    initialTeamId: String,
    private val repository: IEventRepository,
    private val sessionManager: ISessionManager,
    private val generateReportUseCase: GenerateEventsReportUseCase
) : ViewModel() {

    private val _teamId = MutableStateFlow(initialTeamId)

    private val _uiState = MutableStateFlow(EventListState())
    val uiState: StateFlow<EventListState> = _uiState

    init {
        val session = sessionManager.sessionState.value
        val globalRole = session.user?.role
        val teamRole = session.activeTeam?.role
        val canManage = globalRole == UserRole.GERENCIA || 
                        globalRole == UserRole.DIRECTOR_DEPORTIVO || 
                        globalRole == UserRole.SUPERADMIN || 
                        teamRole == "ENTRENADOR"
        _uiState.update { it.copy(canManageEvents = canManage) }
        
        @OptIn(ExperimentalCoroutinesApi::class)
        _teamId.flatMapLatest { id -> repository.observeEvents(id) }
            .onEach { events -> _uiState.update { it.copy(events = events) } }
            .launchIn(viewModelScope)
            
        @OptIn(ExperimentalCoroutinesApi::class)
        _teamId.flatMapLatest { id -> 
            kotlinx.coroutines.flow.flow { emit(repository.hasPlayers(id)) }
        }
            .onEach { playersExist -> _uiState.update { it.copy(hasPlayers = playersExist) } }
            .launchIn(viewModelScope)
            
        viewModelScope.launch {
            _teamId.collect { id ->
                repository.syncEvents(id)
            }
        }
    }
    
    fun setTeamId(newTeamId: String) {
        if (_teamId.value != newTeamId) {
            _teamId.value = newTeamId
        }
    }

    fun createEvent(type: EventType, date: Long, description: String) {
        viewModelScope.launch {
            try {
                if (!_uiState.value.hasPlayers) {
                    _uiState.update { it.copy(error = "No hay jugadores en el equipo. Añade jugadores primero.") }
                    return@launch
                }
                repository.createEvent(_teamId.value, type, date, description)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setShowReportDialog(show: Boolean) {
        _uiState.update { it.copy(showReportDialog = show) }
    }

    fun generateReport(
        format: String,
        eventType: EventType? = null,
        fromDateMillis: Long? = null,
        toDateMillis: Long? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingReport = true, showReportDialog = false) }
            try {
                generateReportUseCase(_teamId.value, format, eventType, fromDateMillis, toDateMillis)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al generar informe: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isGeneratingReport = false) }
            }
        }
    }
}
