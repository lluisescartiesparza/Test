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
import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.feature.teams.domain.model.TeamRole
import com.example.noubasketalzira.feature.teams.domain.model.Team
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val teams by viewModel.teams.collectAsState()
    val users by viewModel.users.collectAsState()
    val canManageTeams by viewModel.canManageTeams.collectAsState()
    
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
    
    var showCreateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var newTeamName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var newTeamCategory by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var teamToDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Team?>(null) }
    
    var teamToAssignMember by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Team?>(null) }
    var selectedUser by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<User?>(null) }
    var selectedTeamRole by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(TeamRole.JUGADOR) }
    var showUserDropdown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showRoleDropdown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

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

            if (canManageTeams) {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Equipo")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

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
                            
                            if (canManageTeams) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(onClick = { 
                                        teamToAssignMember = team
                                        selectedUser = users.firstOrNull()
                                        selectedTeamRole = TeamRole.JUGADOR
                                    }) {
                                        Text("Añadir Miembro")
                                    }
                                    Button(
                                        onClick = { teamToDelete = team },
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
        
        teamToAssignMember?.let { team ->
            AlertDialog(
                onDismissRequest = { teamToAssignMember = null },
                title = { Text("Añadir miembro a ${team.name}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = showUserDropdown,
                            onExpandedChange = { showUserDropdown = !showUserDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedUser?.fullName ?: "Seleccionar Usuario",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Usuario") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUserDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = showUserDropdown,
                                onDismissRequest = { showUserDropdown = false }
                            ) {
                                users.forEach { user ->
                                    DropdownMenuItem(
                                        text = { Text("${user.fullName} (${user.email})") },
                                        onClick = { 
                                            selectedUser = user
                                            showUserDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        ExposedDropdownMenuBox(
                            expanded = showRoleDropdown,
                            onExpandedChange = { showRoleDropdown = !showRoleDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedTeamRole.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Rol en el equipo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRoleDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = showRoleDropdown,
                                onDismissRequest = { showRoleDropdown = false }
                            ) {
                                TeamRole.values().forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.name) },
                                        onClick = { 
                                            selectedTeamRole = role
                                            showRoleDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedUser?.let {
                                viewModel.assignMember(team.id, it.id, selectedTeamRole)
                                teamToAssignMember = null
                            }
                        },
                        enabled = selectedUser != null
                    ) {
                        Text("Asignar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { teamToAssignMember = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
