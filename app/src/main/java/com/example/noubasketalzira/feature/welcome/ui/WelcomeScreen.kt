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
import com.example.noubasketalzira.core.domain.model.UserRole
import org.koin.compose.koinInject

@Composable
fun WelcomeScreen(
    onNavigateToTeams: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToUsers: () -> Unit,
    sessionManager: ISessionManager = koinInject()
) {
    val sessionState by sessionManager.sessionState.collectAsState()
    val currentUser = sessionState.user

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
            text = "Bienvenido, ${currentUser?.fullName ?: ""}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Rol Global: ${currentUser?.role?.name ?: ""}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Only SUPERADMIN, GERENCIA, and DIRECTOR_DEPORTIVO can manage teams and users globally
        val canManageGlobal = currentUser?.role in listOf(
            UserRole.SUPERADMIN,
            UserRole.GERENCIA,
            UserRole.DIRECTOR_DEPORTIVO
        )

        if (canManageGlobal) {
            Button(
                onClick = onNavigateToUsers,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Gestión de Usuarios (Global)")
            }
            
            Button(
                onClick = onNavigateToTeams,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Gestión de Equipos (Global)")
            }
        }

        Button(
            onClick = onNavigateToEvents,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir a mi Equipo (Eventos)")
        }
    }
}
