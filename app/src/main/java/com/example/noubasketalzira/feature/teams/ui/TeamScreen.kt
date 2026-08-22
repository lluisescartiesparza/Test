package com.example.noubasketalzira.feature.teams.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamScreen(
    viewModel: TeamViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val teams by viewModel.teams.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            onClick = { viewModel.createTeam("Nuevo Equipo ${System.currentTimeMillis() % 1000}", "Categoría X") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Equipo (Test)")
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
                            Button(onClick = { viewModel.assignMockMember(team.id) }) {
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
}
