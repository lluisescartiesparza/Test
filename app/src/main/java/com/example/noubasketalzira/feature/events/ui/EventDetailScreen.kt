package com.example.noubasketalzira.feature.events.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noubasketalzira.feature.events.domain.model.Attendance
import com.example.noubasketalzira.feature.events.domain.model.AttendanceStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventDetailViewModel = koinViewModel { parametersOf(eventId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle del Evento") }) },
        floatingActionButton = {
            if (uiState.canManage && uiState.unsummonedPlayers.isNotEmpty()) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Text("+")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.canManage && uiState.canSummonAll) {
                Button(
                    onClick = { viewModel.summonAll() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Convocar a todos")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.summonedPlayers) { attendance ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable(enabled = uiState.canManage) {
                                viewModel.rotateStatus(attendance)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = attendance.userName, style = MaterialTheme.typography.titleMedium)
                                Text(text = attendance.status.name, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (uiState.canManage) {
                                IconButton(onClick = { viewModel.removePlayer(attendance) }) {
                                    Text("X")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Añadir Jugador") },
            text = {
                LazyColumn {
                    items(uiState.unsummonedPlayers) { att ->
                        Text(
                            text = att.userName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.summonPlayer(att)
                                    showAddDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cerrar") } }
        )
    }
}
