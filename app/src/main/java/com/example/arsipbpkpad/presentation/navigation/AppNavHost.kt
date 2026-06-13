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
import com.example.arsipbpkpad.presentation.archive.detail.ArchiveDetailScreen
import com.example.arsipbpkpad.presentation.archive.list.ArchiveListScreen
import com.example.arsipbpkpad.presentation.archive.review.ArchiveReviewScreen
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
                onNavigateToScan = {
                    navController.navigate("archive_flow")
                },
                onNavigateToDetail = { archiveId ->
                    navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                },
                onNavigateToReview = {
                    navController.navigate(Screen.ArchiveReview.route)
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
                        navController.navigate(Screen.RapidInput.route)
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            "home" -> navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                            "archive" -> { /* Already here */ }
                            "add" -> { /* Dialog handles navigation */ }
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
        composable(Screen.ArchiveReview.route) {
            ArchiveReviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.ArchiveList.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
