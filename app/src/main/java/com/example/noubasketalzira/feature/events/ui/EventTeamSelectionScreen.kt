package com.example.noubasketalzira.feature.events.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTeamSelectionScreen(
    onTeamSelected: (String) -> Unit,
    viewModel: EventTeamSelectionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Selecciona un Equipo") }) }
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(uiState.teams) { team ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onTeamSelected(team.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = team.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = team.category ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
