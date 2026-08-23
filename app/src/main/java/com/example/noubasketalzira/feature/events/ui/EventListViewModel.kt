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

data class EventListState(
    val events: List<Event> = emptyList(),
    val canManageEvents: Boolean = false
)

class EventListViewModel(
    private val teamId: String,
    private val repository: IEventRepository,
    private val sessionManager: ISessionManager
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
    }

    fun createEvent(type: EventType, date: Long, description: String) {
        viewModelScope.launch {
            repository.createEvent(teamId, type, date, description)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }
}
