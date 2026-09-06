package com.example.noubasketalzira.feature.teams.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.feature.teams.domain.model.TeamRole
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    viewModel: TeamDetailViewModel = koinViewModel { parametersOf(teamId) },
    onBack: () -> Unit
) {
    val members by viewModel.members.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    
    var memberToDelete by remember { mutableStateOf<String?>(null) }
    var memberToEditRole by remember { mutableStateOf<String?>(null) }
    var editRoleSelection by remember { mutableStateOf(TeamRole.JUGADOR) }
    var showEditRoleDropdown by remember { mutableStateOf(false) }

    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedTeamRole by remember { mutableStateOf(TeamRole.JUGADOR) }
    var showUserDropdown by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedUser = null
                selectedTeamRole = TeamRole.JUGADOR
                showAssignDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Miembro")
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
                Text("Detalles del Equipo", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(members) { member ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = member.user.fullName, style = MaterialTheme.typography.titleMedium)
                                Text(text = member.user.email, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Rol: ${member.role.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    memberToEditRole = member.user.id
                                    editRoleSelection = member.role
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Rol")
                                }
                                IconButton(onClick = { memberToDelete = member.user.id }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Miembro", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        memberToDelete?.let { userId ->
            AlertDialog(
                onDismissRequest = { memberToDelete = null },
                title = { Text("Quitar miembro") },
                text = { Text("¿Estás seguro de que quieres quitar a este miembro del equipo?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.removeMember(userId)
                            memberToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        memberToEditRole?.let { userId ->
            AlertDialog(
                onDismissRequest = { memberToEditRole = null },
                title = { Text("Cambiar Rol") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = showEditRoleDropdown,
                            onExpandedChange = { showEditRoleDropdown = !showEditRoleDropdown }
                        ) {
                            OutlinedTextField(
                                value = editRoleSelection.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Nuevo Rol") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEditRoleDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = showEditRoleDropdown,
                                onDismissRequest = { showEditRoleDropdown = false }
                            ) {
                                TeamRole.values().forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.name) },
                                        onClick = { 
                                            editRoleSelection = role
                                            showEditRoleDropdown = false
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
                            viewModel.updateMemberRole(userId, editRoleSelection)
                            memberToEditRole = null
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToEditRole = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (showAssignDialog) {
            AlertDialog(
                onDismissRequest = { showAssignDialog = false },
                title = { Text("Añadir nuevo miembro") },
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
                                allUsers.forEach { user ->
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
                                label = { Text("Rol") },
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
                                viewModel.assignMember(it.id, selectedTeamRole)
                                showAssignDialog = false
                            }
                        },
                        enabled = selectedUser != null
                    ) {
                        Text("Añadir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAssignDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
