package com.example.noubasketalzira.feature.welcome.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noubasketalzira.core.auth.ISessionManager
import org.koin.compose.koinInject

@Composable
fun WelcomeScreen(
    onNavigateToTeams: () -> Unit,
    onNavigateToEvents: () -> Unit,
    sessionManager: ISessionManager = koinInject()
) {
    val currentUser by sessionManager.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nou Basket Alzira",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Sesión activa: ${currentUser?.role?.name ?: "Ninguna"}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onNavigateToTeams,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Dirección Deportiva (Equipos)")
        }

        Button(
            onClick = onNavigateToEvents,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestión de Equipo (Eventos)")
        }
    }
}
