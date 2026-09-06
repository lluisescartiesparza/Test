package com.example.noubasketalzira.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.noubasketalzira.core.auth.SessionState
import com.example.noubasketalzira.core.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    sessionState: SessionState,
    onTeamSelect: (String) -> Unit,
    onCreateTeam: (String, String) -> Unit = { _, _ -> },
    onDeleteTeam: (String) -> Unit = {},
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val activeTeam = sessionState.activeTeam
    val user = sessionState.user
    
    val shortName = user?.fullName?.split(" ")?.take(2)?.joinToString(" ") ?: ""
    var expanded by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newTeamName by remember { mutableStateOf("") }
    var newTeamCategory by remember { mutableStateOf("") }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    val roleIcon = when (user?.role) {
        UserRole.SUPERADMIN,
        UserRole.GERENCIA,
        UserRole.DIRECTOR_DEPORTIVO -> Icons.Default.AdminPanelSettings
        UserRole.ENTRENADOR -> Icons.Default.Sports
        else -> Icons.Default.Person
    }

    val canManageTeams = user?.role == UserRole.SUPERADMIN || 
                         user?.role == UserRole.GERENCIA || 
                         user?.role == UserRole.DIRECTOR_DEPORTIVO

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = roleIcon, 
                            contentDescription = "Rol",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = shortName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    if (sessionState.userTeams.isNotEmpty() || canManageTeams) {
                        TextButton(onClick = { expanded = true }) {
                            Text(activeTeam?.teamName ?: "Selecciona equipo")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar equipo")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sessionState.userTeams.forEach { userTeam ->
                                DropdownMenuItem(
                                    text = { Text(userTeam.teamName) },
                                    onClick = {
                                        onTeamSelect(userTeam.teamId)
                                        expanded = false
                                    }
                                )
                            }
                            if (canManageTeams) {
                                if (sessionState.userTeams.isNotEmpty()) {
                                    HorizontalDivider()
                                }
                                DropdownMenuItem(
                                    text = { Text("Crear Nuevo Equipo", color = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        expanded = false
                                        showCreateDialog = true
                                    }
                                )
                                if (activeTeam != null) {
                                    DropdownMenuItem(
                                        text = { Text("Borrar Este Equipo", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            expanded = false
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        content(paddingValues)
        
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
                                onCreateTeam(newTeamName, newTeamCategory.takeIf { it.isNotBlank() } ?: "Sin categoría")
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
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar borrado") },
                text = { Text("¿Seguro que deseas eliminar el equipo actual y desvincular a todos sus miembros? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            activeTeam?.teamId?.let { onDeleteTeam(it) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
