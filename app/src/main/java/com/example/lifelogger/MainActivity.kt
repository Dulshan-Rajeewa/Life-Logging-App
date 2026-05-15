package com.example.lifelogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifelogger.data.AppDatabase
import com.example.lifelogger.data.NetworkMonitor
import com.example.lifelogger.ui.AuthViewModel
import com.example.lifelogger.ui.LifeLogViewModel
import com.example.lifelogger.ui.LifeLogViewModelFactory
import com.example.lifelogger.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val logDao = database.logDao()
        val userDao = database.userDao()
        val networkMonitor = NetworkMonitor(this)
        
        val logViewModelFactory = LifeLogViewModelFactory(application, logDao, networkMonitor)
        val authViewModelFactory = com.example.lifelogger.ui.AuthViewModelFactory(userDao)

        // Check for first launch
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("is_first_launch", true)

        setContent {
            val viewModel: LifeLogViewModel = viewModel(factory = logViewModelFactory)
            val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
            
            LifeLoggerApp(viewModel, authViewModel, isFirstLaunch) {
                sharedPrefs.edit().putBoolean("is_first_launch", false).apply()
            }
        }
    }
}

@Composable
fun LifeLoggerApp(
    viewModel: LifeLogViewModel, 
    authViewModel: AuthViewModel, 
    isFirstLaunch: Boolean,
    onFirstLaunchComplete: () -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val syncMessage by viewModel.syncMessage.collectAsState()

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController, 
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashScreen(onFinished = {
                    if (isFirstLaunch) {
                        navController.navigate("auth") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                })
            }

            composable("auth") {
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {
                        onFirstLaunchComplete()
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                LifeLoggerHomeScreen(
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    onAddClick = { navController.navigate("add_entry") },
                    onViewAllClick = { navController.navigate("all_logs") },
                    onLogClick = { logId -> navController.navigate("log_detail/$logId") },
                    onNavigateToAuth = {
                        navController.navigate("auth") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("add_entry") {
                val currentUserState = authViewModel.currentUser.collectAsState()
                AddEntryScreen(
                    viewModel = viewModel,
                    userId = currentUserState.value?.id,
                    onBackClick = { 
                        navController.popBackStack()
                        // Since we can't easily pass data back via popBackStack without extra logic,
                        // we'll rely on the ViewModel to trigger a "success" state if needed.
                    }
                )
            }

            composable("all_logs") {
                AllLogsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogClick = { logId -> navController.navigate("log_detail/$logId") }
                )
            }

            composable("log_detail/{logId}") { backStackEntry ->
                val logId = backStackEntry.arguments?.getString("logId") ?: return@composable
                LogDetailScreen(
                    logId = logId,
                    viewModel = viewModel,
                    userId = authViewModel.currentUser.collectAsState().value?.id,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("edit_entry/$id") },
                    onDeleteSuccess = { navController.popBackStack() }
                )
            }

            composable("edit_entry/{logId}") { backStackEntry ->
                val logId = backStackEntry.arguments?.getString("logId")
                AddEntryScreen(
                    logId = logId,
                    viewModel = viewModel,
                    userId = authViewModel.currentUser.collectAsState().value?.id,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}