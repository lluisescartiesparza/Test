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
    private val teamId: String,
    private val repository: IEventRepository,
    private val sessionManager: ISessionManager,
    private val generateReportUseCase: GenerateEventsReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventListState())
    val uiState: StateFlow<EventListState> = _uiState

    init {
        val user = sessionManager.currentUser.value
        val canManage = user?.role != null && user.role != UserRole.JUGADOR
        _uiState.update { it.copy(canManageEvents = canManage) }
        
        viewModelScope.launch {
            repository.observeEvents(teamId).collect { events ->
                _uiState.update { it.copy(events = events) }
            }
        }
        
        viewModelScope.launch {
            val playersExist = repository.hasPlayers(teamId)
            _uiState.update { it.copy(hasPlayers = playersExist) }
        }
        
        viewModelScope.launch {
            repository.syncEvents(teamId)
        }
    }

    fun createEvent(type: EventType, date: Long, description: String) {
        viewModelScope.launch {
            try {
                if (!_uiState.value.hasPlayers) {
                    _uiState.update { it.copy(error = "No hay jugadores en el equipo. Añade jugadores primero.") }
                    return@launch
                }
                repository.createEvent(teamId, type, date, description)
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
                generateReportUseCase(teamId, format, eventType, fromDateMillis, toDateMillis)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al generar informe: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isGeneratingReport = false) }
            }
        }
    }
}
