package com.example.noubasketalzira

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.noubasketalzira.feature.welcome.ui.WelcomeScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = Modifier.safeDrawingPadding()
    ) {
        composable("welcome") {
            WelcomeScreen(
                onNavigateToTeams = { navController.navigate("teams") },
                onNavigateToEvents = { navController.navigate("events") }
            )
        }
        
        composable("teams") {
            // Provisional empty route for "Dirección Deportiva (Equipos)"
        }
        
        composable("events") {
            // Provisional empty route for "Gestión de Equipo (Eventos)"
        }
    }
}
