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
import com.example.arsipbpkpad.presentation.archive.detail.ArchiveDetailScreen
import com.example.arsipbpkpad.presentation.archive.list.ArchiveListScreen
import com.example.arsipbpkpad.presentation.home.screen.HomeScreen
import com.example.arsipbpkpad.presentation.scan.ScanScreen
import com.example.arsipbpkpad.presentation.archive.add.manual.BoxContextScreen
import com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputScreen
import com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputViewModel

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
                onNavigateToScan = {
                    navController.navigate("archive_flow") {
                        // When clicking ADD on Home, we want to go straight to initialization
                        // But since ArchiveList handles it, we go to flow which lands on List.
                        // Or we can navigate directly to BoxContext if we move it out.
                    }
                },
                onNavigateToDetail = { archiveId ->
                    navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                },
                onNavigateToReview = {
                    navController.navigate("archive_flow") // Shortcut to current session
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
                    viewModel = hiltViewModel(),
                    stagingViewModel = rapidViewModel,
                    onNavigateToDetail = { archiveId ->
                        navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                    },
                    onNavigateToRapidInput = {
                        navController.navigate(Screen.RapidInput.route)
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            "home" -> navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                            "archive" -> { /* Already here */ }
                            "add" -> navController.navigate(Screen.BoxContext.route)
                        }
                    }
                )
            }
            
            composable(Screen.BoxContext.route) { entry ->
                val flowEntry = remember(entry) { navController.getBackStackEntry("archive_flow") }
                val rapidViewModel: RapidInputViewModel = hiltViewModel(flowEntry)

                BoxContextScreen(
                    viewModel = rapidViewModel,
                    onNavigateToRapidInput = {
                        navController.navigate(Screen.RapidInput.route)
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            "home" -> navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                            "archive" -> navController.navigate(Screen.ArchiveList.route)
                            "add" -> { /* Already here */ }
                        }
                    }
                )
            }
            
            composable(Screen.RapidInput.route) { entry ->
                val flowEntry = remember(entry) { navController.getBackStackEntry("archive_flow") }
                val rapidViewModel: RapidInputViewModel = hiltViewModel(flowEntry)

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
