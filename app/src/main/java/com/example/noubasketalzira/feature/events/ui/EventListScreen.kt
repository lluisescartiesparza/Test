package com.example.noubasketalzira.feature.events.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*
import com.example.noubasketalzira.feature.events.domain.model.EventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    teamId: String,
    onEventSelected: (String) -> Unit,
    viewModel: EventListViewModel = koinViewModel { parametersOf(teamId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Eventos del Equipo") }) },
        floatingActionButton = {
            if (uiState.canManageEvents) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Text("+")
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(uiState.events) { event ->
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(event.date))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onEventSelected(event.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = event.type.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = dateStr, style = MaterialTheme.typography.bodyMedium)
                        if (!event.description.isNullOrEmpty()) {
                            Text(text = event.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { type, desc -> 
                viewModel.createEvent(type, desc)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onCreate: (EventType, String) -> Unit
) {
    var type by remember { mutableStateOf(EventType.ENTRENAMIENTO) }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Evento") },
        text = {
            Column {
                Row {
                    RadioButton(selected = type == EventType.ENTRENAMIENTO, onClick = { type = EventType.ENTRENAMIENTO })
                    Text("Entrenamiento", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = type == EventType.PARTIDO, onClick = { type = EventType.PARTIDO })
                    Text("Partido")
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(type, description) }) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
