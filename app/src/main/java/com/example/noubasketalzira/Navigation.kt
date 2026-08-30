package com.example.noubasketalzira

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.ui.MainScaffold
import com.example.noubasketalzira.feature.welcome.ui.WelcomeScreen
import org.koin.compose.koinInject

@Composable
fun MainNavigation() {
    val sessionManager: ISessionManager = koinInject()
    val sessionState by sessionManager.sessionState.collectAsState()

    if (!sessionState.isLoggedIn) {
        val authNavController = rememberNavController()
        NavHost(navController = authNavController, startDestination = "login") {
            composable("login") {
                com.example.noubasketalzira.feature.auth.ui.LoginScreen(
                    onNavigateToOtp = { email -> authNavController.navigate("otp/$email") }
                )
            }
            composable("otp/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                com.example.noubasketalzira.feature.auth.ui.OtpScreen(
                    email = email,
                    onBack = { authNavController.popBackStack() }
                )
            }
        }
    } else {
        val coroutineScope = rememberCoroutineScope()
        MainScaffold(
            sessionState = sessionState,
            onTeamSelect = { sessionManager.setActiveTeam(it) },
            onLogout = { coroutineScope.launch { sessionManager.logout() } }
        ) { paddingValues ->
            if (sessionState.activeTeam == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "Por favor, selecciona un equipo en el menú superior",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "welcome",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onNavigateToTeams = { navController.navigate("teams") },
                            onNavigateToEvents = { navController.navigate("events") },
                            onNavigateToUsers = { navController.navigate("users") }
                        )
                    }

                    composable("teams") {
                        com.example.noubasketalzira.feature.teams.ui.TeamScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("users") {
                        com.example.noubasketalzira.feature.users.ui.UserManagementScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("events") {
                        val activeTeam = sessionState.activeTeam
                        if (activeTeam != null) {
                            com.example.noubasketalzira.feature.events.ui.EventListScreen(
                                teamId = activeTeam.teamId,
                                onEventSelected = { eventId -> navController.navigate("eventDetail/$eventId") }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Selecciona un equipo primero")
                            }
                        }
                    }

                    composable("eventDetail/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        com.example.noubasketalzira.feature.events.ui.EventDetailScreen(
                            eventId = eventId
                        )
                    }
                }
            }
        }
    }
}
