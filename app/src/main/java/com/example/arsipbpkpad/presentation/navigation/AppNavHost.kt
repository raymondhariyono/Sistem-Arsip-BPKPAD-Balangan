package com.example.arsipbpkpad.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputScreen
import com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputViewModel
import com.example.arsipbpkpad.presentation.archive.add.manual.StagingBoxListScreen
import com.example.arsipbpkpad.presentation.archive.detail.ArchiveDetailScreen
import com.example.arsipbpkpad.presentation.archive.list.ArchiveListScreen
import com.example.arsipbpkpad.presentation.home.screen.HomeScreen
import com.example.arsipbpkpad.presentation.scan.ScanScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToArchiveList = {
                    navController.navigate("archive_flow")
                },
                onNavigateToStagingBoxList = {
                    navController.navigate(Screen.StagingBoxList.route)
                },
                onNavigateToDetail = { archiveId ->
                    navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                },
                onNavigateToRapidInput = { sessionId ->
                    navController.navigate(Screen.RapidInput.createRoute(sessionId))
                }
            )
        }

        navigation(
            startDestination = Screen.ArchiveList.route,
            route = "archive_flow"
        ) {
            composable(Screen.ArchiveList.route) { entry ->
                val flowEntry = remember(entry) { navController.getBackStackEntry("archive_flow") }
                val rapidViewModel: RapidInputViewModel = hiltViewModel(flowEntry)
                
                ArchiveListScreen(
                    stagingViewModel = rapidViewModel,
                    onNavigateToDetail = { archiveId ->
                        navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                    },
                    onNavigateToRapidInput = {
                        // This usually comes from the dialog which already set the session in VM
                        // But let's be explicit and pass the current session from state
                        val sessionId = rapidViewModel.uiState.value.currentSessionId ?: ""
                        navController.navigate(Screen.RapidInput.createRoute(sessionId))
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            "home" -> navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                            "archive" -> { /* Already here */ }
                            "add" -> navController.navigate(Screen.StagingBoxList.route)
                        }
                    }
                )
            }

            composable(Screen.StagingBoxList.route) { entry ->
                val flowEntry = remember(entry) { navController.getBackStackEntry("archive_flow") }
                val rapidViewModel: RapidInputViewModel = hiltViewModel(flowEntry)

                StagingBoxListScreen(
                    viewModel = rapidViewModel,
                    onNavigateToRapidInput = { sessionId ->
                        navController.navigate(Screen.RapidInput.createRoute(sessionId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.RapidInput.route) { entry ->
                val sessionId = entry.arguments?.getString("sessionId") ?: ""
                val flowEntry = remember(entry) { navController.getBackStackEntry("archive_flow") }
                val rapidViewModel: RapidInputViewModel = hiltViewModel(flowEntry)

                // Ensure the session is set in the shared ViewModel
                androidx.compose.runtime.LaunchedEffect(sessionId) {
                    if (sessionId.isNotEmpty()) {
                        rapidViewModel.onEvent(com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputUiEvent.SetCurrentSession(sessionId))
                    }
                }

                RapidInputScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = rapidViewModel
                )
            }
        }

        composable(Screen.ArchiveDetail.route) { backStackEntry ->
            val archiveId = backStackEntry.arguments?.getString("archiveId") ?: ""
            ArchiveDetailScreen(
                archiveId = archiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
