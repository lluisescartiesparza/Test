package com.example.noubasketalzira.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.noubasketalzira.core.auth.SessionState

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    sessionState: SessionState,
    onTeamSelect: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val activeTeam = sessionState.activeTeam
    val user = sessionState.user
    
    val shortName = user?.fullName?.split(" ")?.take(2)?.joinToString(" ") ?: ""
    var expanded by remember { mutableStateOf(false) }

    val roleIcon = when (user?.role) {
        com.example.noubasketalzira.core.domain.model.UserRole.SUPERADMIN,
        com.example.noubasketalzira.core.domain.model.UserRole.GERENCIA,
        com.example.noubasketalzira.core.domain.model.UserRole.DIRECTOR_DEPORTIVO -> Icons.Default.AdminPanelSettings
        com.example.noubasketalzira.core.domain.model.UserRole.ENTRENADOR -> Icons.Default.Sports
        else -> Icons.Default.Person
    }

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
                    if (sessionState.userTeams.isNotEmpty()) {
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
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}
