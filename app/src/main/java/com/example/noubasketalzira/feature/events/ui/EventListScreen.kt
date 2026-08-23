package com.example.noubasketalzira.feature.events.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*
import com.example.noubasketalzira.feature.events.domain.model.EventType

import com.example.noubasketalzira.core.ui.debouncedClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    teamId: String,
    onEventSelected: (String) -> Unit,
    viewModel: EventListViewModel = koinViewModel { parametersOf(teamId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Eventos del Equipo") }) },
        floatingActionButton = {
            if (uiState.canManageEvents) {
                FloatingActionButton(onClick = { 
                    if (!uiState.hasPlayers) {
                        viewModel.createEvent(EventType.ENTRENAMIENTO, 0, "") // Will trigger error
                    } else {
                        showCreateDialog = true 
                    }
                }) {
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
                        .debouncedClickable { onEventSelected(event.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = event.type.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = dateStr, style = MaterialTheme.typography.bodyMedium)
                            if (!event.description.isNullOrEmpty()) {
                                Text(text = event.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (uiState.canManageEvents) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE53935), CircleShape) // Red background
                                    .clickable { eventToDelete = event.id } // keeping regular clickable for delete icon as it's small and less prone to click-through navigation issues, or could change it too. Let's keep it.
                                    .padding(12.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Borrar Evento") },
            text = { Text("¿Estás seguro de que quieres borrar este evento? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvent(eventToDelete!!)
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { type, date, desc -> 
                viewModel.createEvent(type, date, desc)
                showCreateDialog = false
            }
        )
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Aviso") },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onCreate: (EventType, Long, String) -> Unit
) {
    var type by remember { mutableStateOf(EventType.ENTRENAMIENTO) }
    var description by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Time states
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Evento") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == EventType.ENTRENAMIENTO, onClick = { type = EventType.ENTRENAMIENTO })
                    Text("Entrenamiento", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = type == EventType.PARTIDO, onClick = { type = EventType.PARTIDO })
                    Text("Partido")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { showDatePicker = true }) {
                        val selectedMillis = datePickerState.selectedDateMillis
                        val dateText = if (selectedMillis != null) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedMillis))
                        } else {
                            "Fecha"
                        }
                        Text(dateText)
                    }

                    Button(onClick = { 
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedHour = hour
                                selectedMinute = minute
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }) {
                        val timeText = if (selectedHour != null && selectedMinute != null) {
                            String.format("%02d:%02d", selectedHour, selectedMinute)
                        } else {
                            "Hora"
                        }
                        Text(timeText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = dateMillis
                    
                    if (selectedHour != null && selectedMinute != null) {
                        cal.set(Calendar.HOUR_OF_DAY, selectedHour!!)
                        cal.set(Calendar.MINUTE, selectedMinute!!)
                    }
                    
                    onCreate(type, cal.timeInMillis, description) 
                },
                enabled = datePickerState.selectedDateMillis != null && selectedHour != null
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
