package com.example.noubasketalzira.feature.teams.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamScreen(
    viewModel: TeamViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val teams by viewModel.teams.collectAsState()
    
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var showCreateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var newTeamName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var newTeamCategory by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onBack) {
                    Text("Volver")
                }
                Text("Gestión de Equipos", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Equipo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teams) { team ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = team.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Categoría: ${team.category ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { 
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Gestión de miembros provisionalmente desde el panel web")
                                    }
                                }) {
                                    Text("Asignar Entrenador")
                                }
                                Button(
                                    onClick = { viewModel.deleteTeam(team) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Borrar")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Crear Nuevo Equipo") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTeamName,
                            onValueChange = { newTeamName = it },
                            label = { Text("Nombre del equipo") }
                        )
                        OutlinedTextField(
                            value = newTeamCategory,
                            onValueChange = { newTeamCategory = it },
                            label = { Text("Categoría (opcional)") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTeamName.isNotBlank()) {
                                viewModel.createTeam(newTeamName, newTeamCategory.takeIf { it.isNotBlank() } ?: "Sin categoría")
                                newTeamName = ""
                                newTeamCategory = ""
                                showCreateDialog = false
                            }
                        }
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
