package com.example.noubasketalzira.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.noubasketalzira.core.auth.SessionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    sessionState: SessionState,
    onTeamSelect: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val activeTeam = sessionState.activeTeam
    val user = sessionState.user
    
    val shortName = user?.fullName?.split(" ")?.take(2)?.joinToString(" ") ?: ""
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$shortName - ${activeTeam?.role ?: ""}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
