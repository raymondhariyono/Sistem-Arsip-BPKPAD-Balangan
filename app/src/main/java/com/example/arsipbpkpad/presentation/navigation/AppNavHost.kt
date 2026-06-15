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
import com.example.arsipbpkpad.presentation.analytics.AnalyticsScreen
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
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                },
                onNavigateToScan = {
                    navController.navigate(Screen.Scan.route)
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
                        val sessionId = rapidViewModel.uiState.value.currentSessionId ?: ""
                        navController.navigate(Screen.RapidInput.createRoute(sessionId))
                    },
                    onNavigateToScan = {
                        navController.navigate(Screen.Scan.route)
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            "home" -> navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                            "archive" -> { /* Already here */ }
                            "add" -> navController.navigate(Screen.StagingBoxList.route)
                            "analytics" -> navController.navigate(Screen.Analytics.route)
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

            composable(Screen.Scan.route) { entry ->
                val flowEntry = remember(entry) { navController.getBackStackEntry("archive_flow") }
                val rapidViewModel: RapidInputViewModel = hiltViewModel(flowEntry)

                ScanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReview = { imageUri, docNumber, year, subject ->
                        // Pre-fill the shared ViewModel
                        rapidViewModel.onEvent(com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputUiEvent.OnPreFillFromOcr(imageUri, docNumber, year, subject))
                        
                        // Navigate to Rapid Input. 
                        // If no session exists, we might need to create one or go to StagingBoxList first.
                        val currentSessionId = rapidViewModel.uiState.value.currentSessionId
                        if (currentSessionId != null) {
                            navController.navigate(Screen.RapidInput.createRoute(currentSessionId)) {
                                popUpTo(Screen.Scan.route) { inclusive = true }
                            }
                        } else {
                            // No session, go to Box selection first but keep the pre-filled data in VM
                            navController.navigate(Screen.StagingBoxList.route) {
                                popUpTo(Screen.Scan.route) { inclusive = true }
                            }
                        }
                    }
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

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateToBottomNav = { item ->
                    when (item.route) {
                        "home" -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        "archive" -> navController.navigate("archive_flow") {
                            popUpTo(Screen.Home.route)
                        }
                        "add" -> navController.navigate(Screen.StagingBoxList.route)
                        "analytics" -> { /* Already here */ }
                    }
                }
            )
        }
    }
}
