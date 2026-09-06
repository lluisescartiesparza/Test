package com.example.noubasketalzira.feature.teams.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noubasketalzira.feature.teams.domain.model.Team
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = koinViewModel(),
    onNavigateToTeamDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val teams by viewModel.teams.collectAsState()
    val canManageTeams by viewModel.canManageTeams.collectAsState()
    
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
    
    var showCreateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var newTeamName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var newTeamCategory by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var teamToDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Team?>(null) }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (canManageTeams) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Equipo")
                }
            }
        }
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(teams) { team ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = team.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Categoría: ${team.category ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            }
                            
                            if (canManageTeams) {
                                Row {
                                    IconButton(onClick = { onNavigateToTeamDetail(team.id) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar Equipo")
                                    }
                                    IconButton(onClick = { teamToDelete = team }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar Equipo", tint = MaterialTheme.colorScheme.error)
                                    }
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

        teamToDelete?.let { team ->
            AlertDialog(
                onDismissRequest = { teamToDelete = null },
                title = { Text("Confirmar borrado") },
                text = { Text("¿Seguro que deseas eliminar este equipo?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTeam(team)
                            teamToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { teamToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
