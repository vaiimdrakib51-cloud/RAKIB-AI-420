package com.nova.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nova.ai.presentation.activity.ActivityScreen
import com.nova.ai.presentation.assistant.AssistantScreen
import com.nova.ai.presentation.automation.AutomationScreen
import com.nova.ai.presentation.components.NovaBottomNavBar
import com.nova.ai.presentation.navigation.Screen
import com.nova.ai.presentation.settings.SettingsScreen
import com.nova.ai.presentation.theme.NovaAITheme
import com.nova.ai.presentation.theme.NovaBackground
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaAITheme {
                NovaAppRoot()
            }
        }
    }
}

@Composable
fun NovaAppRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NovaBackground,
        bottomBar = {
            NovaBottomNavBar(
                currentRoute = currentRoute,
                onItemClick = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Assistant.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Assistant.route) {
                AssistantScreen()
            }
            composable(Screen.Automation.route) {
                AutomationScreen()
            }
            composable(Screen.Activity.route) {
                ActivityScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
