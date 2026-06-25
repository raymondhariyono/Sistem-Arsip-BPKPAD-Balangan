package com.example.arsipbpkpad.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.UserRole
import com.example.arsipbpkpad.presentation.analytics.AnalyticsScreen
import com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputScreen
import com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputUiEvent
import com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputViewModel
import com.example.arsipbpkpad.presentation.archive.add.manual.StagingBoxListScreen
import com.example.arsipbpkpad.presentation.archive.detail.ArchiveDetailScreen
import com.example.arsipbpkpad.presentation.archive.list.ArchiveListScreen
import com.example.arsipbpkpad.presentation.auth.screen.LoginScreen
import com.example.arsipbpkpad.presentation.home.screen.HomeScreen
import com.example.arsipbpkpad.presentation.scan.ScanScreen
import com.example.arsipbpkpad.presentation.storage.BoxManagementScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: com.example.arsipbpkpad.presentation.auth.LoginViewModel = hiltViewModel()
) {
    val authRepository = authViewModel.authRepository
    val isUserLoggedIn by authRepository.isUserLoggedIn.collectAsStateWithLifecycle()
    val currentUserRole by authRepository.currentUserRole.collectAsStateWithLifecycle()
    
    val startDestination = if (isUserLoggedIn) Screen.Home.route else Screen.Login.route

    val archiveFlowRoute = stringResource(R.string.route_archive_flow)
    val navHomeId = stringResource(R.string.nav_home_id)
    val navArchiveId = stringResource(R.string.nav_archive_id)
    val navStorageId = "box_management"
    val navAddId = stringResource(R.string.nav_add_id)
    val navAnalyticsId = stringResource(R.string.nav_analytics_id)
    val sessionIdKey = stringResource(R.string.key_session_id)
    val archiveIdKey = stringResource(R.string.key_archive_id)
    val ocrResultKey = stringResource(R.string.key_ocr_result)

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userRole = currentUserRole,
                onNavigateToArchiveList = { year ->
                    navController.navigate(Screen.ArchiveList.createRoute(year))
                },
                onNavigateToDetail = { archiveId ->
                    navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                },
                onNavigateToStagingBoxList = {
                    if (currentUserRole == UserRole.ARSIPARIS) {
                        navController.navigate(Screen.StagingBoxList.route)
                    }
                },
                onNavigateToRapidInput = { sessionId ->
                    if (currentUserRole == UserRole.ARSIPARIS) {
                        navController.navigate(Screen.RapidInput.createRoute(sessionId))
                    }
                },
                onNavigateToAnalytics = {
                    if (currentUserRole != UserRole.ARSIPARIS) {
                        navController.navigate(Screen.Analytics.route)
                    }
                },
                onNavigateToScan = {
                    if (currentUserRole == UserRole.ARSIPARIS) {
                        navController.navigate(Screen.Scan.route)
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        navigation(
            startDestination = Screen.ArchiveList.route,
            route = archiveFlowRoute
        ) {
            composable(
                route = Screen.ArchiveList.route,
                arguments = listOf(
                    androidx.navigation.navArgument("year") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                val flowEntry = remember(entry) { navController.getBackStackEntry(archiveFlowRoute) }
                val rapidInputViewModel: RapidInputViewModel = hiltViewModel(flowEntry)
                val archiveListViewModel: com.example.arsipbpkpad.presentation.archive.list.ArchiveListViewModel = hiltViewModel(flowEntry)
                val year = entry.arguments?.getString("year")?.toIntOrNull()

                ArchiveListScreen(
                    year = year,
                    userRole = currentUserRole,
                    viewModel = archiveListViewModel,
                    stagingViewModel = rapidInputViewModel,
                    onNavigateToDetail = { archiveId ->
                        navController.navigate(Screen.ArchiveDetail.createRoute(archiveId))
                    },
                    onNavigateToRapidInput = {
                        if (currentUserRole == UserRole.ARSIPARIS) {
                            navController.navigate(Screen.RapidInput.route)
                        }
                    },
                    onNavigateToScan = {
                        if (currentUserRole == UserRole.ARSIPARIS) {
                            navController.navigate(Screen.Scan.route)
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            navHomeId -> navController.navigate(Screen.Home.route)
                            navArchiveId -> { /* Already here */ }
                            navStorageId -> navController.navigate(Screen.BoxManagement.route)
                            navAddId -> {
                                if (currentUserRole == UserRole.ARSIPARIS) {
                                    navController.navigate(Screen.StagingBoxList.route)
                                }
                            }
                            navAnalyticsId -> {
                                if (currentUserRole != UserRole.ARSIPARIS) {
                                    navController.navigate(Screen.Analytics.route)
                                }
                            }
                        }
                    }
                )
            }

            composable(Screen.ArchiveDetail.route) { entry ->
                val archiveId = entry.arguments?.getString(archiveIdKey) ?: ""
                ArchiveDetailScreen(
                    archiveId = archiveId,
                    userRole = currentUserRole,
                    onNavigateBack = {
                        navController.navigate(Screen.ArchiveList.route) {
                            popUpTo(Screen.ArchiveList.route) {
                                inclusive = false
                            }
                        }
                    },
                    onNavigateToArchive = { id ->
                        navController.navigate(Screen.ArchiveDetail.createRoute(id))
                    },
                    onNavigateToEdit = { id ->
                        if (currentUserRole == UserRole.ARSIPARIS) {
                            navController.navigate(Screen.EditArchive.createRoute(id))
                        }
                    }
                )
            }

            composable(Screen.StagingBoxList.route) { entry ->
                if (currentUserRole != UserRole.ARSIPARIS) {
                    navController.popBackStack()
                    return@composable
                }
                
                val rapidInputViewModel: RapidInputViewModel = hiltViewModel(
                    remember(entry) { navController.getBackStackEntry(archiveFlowRoute) }
                )
                StagingBoxListScreen(
                    viewModel = rapidInputViewModel,
                    userRole = currentUserRole,
                    onNavigateToRapidInput = { sessionId ->
                        navController.navigate(Screen.RapidInput.createRoute(sessionId))
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            navHomeId -> navController.navigate(Screen.Home.route)
                            navArchiveId -> navController.navigate(archiveFlowRoute)
                            navStorageId -> navController.navigate(Screen.BoxManagement.route)
                            navAddId -> { /* Already here */ }
                            navAnalyticsId -> {
                                if (currentUserRole != UserRole.ARSIPARIS) {
                                    navController.navigate(Screen.Analytics.route)
                                }
                            }
                        }
                    }
                )
            }

            composable(Screen.RapidInput.route) { entry ->
                if (currentUserRole != UserRole.ARSIPARIS) {
                    navController.popBackStack()
                    return@composable
                }
                
                val sessionId = entry.arguments?.getString(sessionIdKey) ?: ""
                val flowEntry = remember(entry) { navController.getBackStackEntry(archiveFlowRoute) }
                val rapidInputViewModel: RapidInputViewModel = hiltViewModel(flowEntry)
                
                // Observe OCR results from multiple possible handles to ensure delivery
                val ocrResult by entry.savedStateHandle.getStateFlow<com.example.arsipbpkpad.domain.model.ParsedMetadata?>(ocrResultKey, null).collectAsStateWithLifecycle()
                val flowOcrResult by flowEntry.savedStateHandle.getStateFlow<com.example.arsipbpkpad.domain.model.ParsedMetadata?>(ocrResultKey, null).collectAsStateWithLifecycle()

                LaunchedEffect(ocrResult) {
                    ocrResult?.let {
                        rapidInputViewModel.onEvent(RapidInputUiEvent.OnOcrResultReceived(it))
                        entry.savedStateHandle[ocrResultKey] = null
                    }
                }
                
                LaunchedEffect(flowOcrResult) {
                    flowOcrResult?.let {
                        rapidInputViewModel.onEvent(RapidInputUiEvent.OnOcrResultReceived(it))
                        flowEntry.savedStateHandle[ocrResultKey] = null
                    }
                }

                RapidInputScreen(
                    sessionId = sessionId,
                    userRole = currentUserRole,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScan = {
                        navController.navigate(Screen.Scan.route)
                    },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            navHomeId -> navController.navigate(Screen.Home.route)
                            navArchiveId -> navController.navigate(archiveFlowRoute)
                            navStorageId -> navController.navigate(Screen.BoxManagement.route)
                            navAddId -> navController.navigate(Screen.StagingBoxList.route)
                            navAnalyticsId -> {
                                if (currentUserRole != UserRole.ARSIPARIS) {
                                    navController.navigate(Screen.Analytics.route)
                                }
                            }
                        }
                    },
                    viewModel = rapidInputViewModel
                )
            }

            composable(
                route = Screen.EditArchive.route,
                arguments = listOf(
                    androidx.navigation.navArgument("archiveId") {
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { entry ->
                if (currentUserRole != UserRole.ARSIPARIS) {
                    navController.popBackStack()
                    return@composable
                }
                
                val rapidInputViewModel: RapidInputViewModel = hiltViewModel()

                val ocrResult by entry.savedStateHandle.getStateFlow<com.example.arsipbpkpad.domain.model.ParsedMetadata?>(ocrResultKey, null).collectAsStateWithLifecycle()

                LaunchedEffect(ocrResult) {
                    ocrResult?.let {
                        rapidInputViewModel.onEvent(RapidInputUiEvent.OnOcrResultReceived(it))
                        entry.savedStateHandle[ocrResultKey] = null
                    }
                }

                RapidInputScreen(
                    sessionId = "",
                    userRole = currentUserRole,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScan = {
                        navController.navigate(Screen.Scan.route)
                    },
                    onNavigateToBottomNav = { item ->
                        when (item.route) {
                            navHomeId -> navController.navigate(Screen.Home.route)
                            navArchiveId -> navController.navigate(archiveFlowRoute)
                            navStorageId -> navController.navigate(Screen.BoxManagement.route)
                            navAddId -> navController.navigate(Screen.StagingBoxList.route)
                            navAnalyticsId -> {
                                if (currentUserRole != UserRole.ARSIPARIS) {
                                    navController.navigate(Screen.Analytics.route)
                                }
                            }
                        }
                    },
                    viewModel = rapidInputViewModel
                )
            }
        }

        composable(Screen.Scan.route) {
            if (currentUserRole != UserRole.ARSIPARIS) {
                navController.popBackStack()
                return@composable
            }
            
            ScanScreen(
                onNavigateBack = { navController.popBackStack() },
                onResultDispatched = { metadata ->
                    // Set result on previous backstack entry (RapidInput)
                    navController.previousBackStackEntry?.savedStateHandle?.set(ocrResultKey, metadata)
                    
                    // Also set on flow entry to ensure delivery if the above fails
                    try {
                        val flowEntry = navController.getBackStackEntry(archiveFlowRoute)
                        flowEntry.savedStateHandle[ocrResultKey] = metadata
                    } catch (e: Exception) {
                        android.util.Log.e("AppNavHost", "archive_flow not found during scan dispatch")
                    }

                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Analytics.route) {
            if (currentUserRole == UserRole.ARSIPARIS) {
                navController.popBackStack()
                return@composable
            }
            
            AnalyticsScreen(
                userRole = currentUserRole,
                onNavigateToBottomNav = { item ->
                    when (item.route) {
                        navHomeId -> navController.navigate(Screen.Home.route)
                        navArchiveId -> navController.navigate(archiveFlowRoute)
                        navStorageId -> navController.navigate(Screen.BoxManagement.route)
                        navAddId -> {
                            if (currentUserRole == UserRole.ARSIPARIS) {
                                navController.navigate(Screen.StagingBoxList.route)
                            }
                        }
                        navAnalyticsId -> { /* Already here */ }
                    }
                }
            )
        }

        composable(Screen.BoxManagement.route) {
            BoxManagementScreen(
                userRole = currentUserRole,
                onNavigateToBottomNav = { item ->
                    when (item.route) {
                        navHomeId -> navController.navigate(Screen.Home.route)
                        navArchiveId -> navController.navigate(archiveFlowRoute)
                        navStorageId -> { /* Already here */ }
                        navAddId -> {
                            if (currentUserRole == UserRole.ARSIPARIS) {
                                navController.navigate(Screen.StagingBoxList.route)
                            }
                        }
                        navAnalyticsId -> {
                            if (currentUserRole != UserRole.ARSIPARIS) {
                                navController.navigate(Screen.Analytics.route)
                            }
                        }
                    }
                }
            )
        }
    }
}
