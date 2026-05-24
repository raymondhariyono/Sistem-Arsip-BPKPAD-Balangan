package com.example.arsipbpkpad.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arsipbpkpad.presentation.archive.add.AddArchiveScreen
import com.example.arsipbpkpad.presentation.archive.add.excelimport.ImportScreen
import com.example.arsipbpkpad.presentation.archive.add.manual.ManualAddScreen
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
                    navController.navigate(Screen.ArchiveList.route)
                },
                onNavigateToScan = {
                    navController.navigate(Screen.AddArchive.route)
                },
                onNavigateToDetail = { archiveId ->
                    navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                }
            )
        }
        composable(Screen.ArchiveList.route) {
            ArchiveListScreen(
                onNavigateToDetail = { archiveId ->
                    navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBottomNav = { item ->
                    when (item.route) {
                        "home" -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        "archive" -> { /* Already here */ }
                        "add" -> navController.navigate(Screen.AddArchive.route)
                    }
                }
            )
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddArchive.route) {
            AddArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanOcr = { navController.navigate(Screen.Scan.route) },
                onNavigateToManualAdd = { navController.navigate(Screen.ManualAdd.route) },
                onNavigateToImportSpreadsheet = { navController.navigate(Screen.Import.route) },
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
        composable(Screen.ManualAdd.route) {
            ManualAddScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Import.route) {
            ImportScreen(
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
    }
}
