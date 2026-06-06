package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.NexusViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainDashboard
import com.example.ui.screens.SpaceChatScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: NexusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. Entrance registration screen
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Main Tabbed Dashboard (Chats, Spaces, Search, Profile)
                        composable("dashboard") {
                            MainDashboard(
                                viewModel = viewModel,
                                onNavigateToChat = { partnerUsername ->
                                    navController.navigate("chat/$partnerUsername")
                                },
                                onNavigateToSpace = { spaceId ->
                                    navController.navigate("space/$spaceId")
                                },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Private 1-on-1 Chat view
                        composable("chat/{username}") { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: ""
                            ChatScreen(
                                viewModel = viewModel,
                                partnerUsername = username,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 4. Space public Chat view
                        composable("space/{spaceId}") { backStackEntry ->
                            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: ""
                            SpaceChatScreen(
                                viewModel = viewModel,
                                spaceId = spaceId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
