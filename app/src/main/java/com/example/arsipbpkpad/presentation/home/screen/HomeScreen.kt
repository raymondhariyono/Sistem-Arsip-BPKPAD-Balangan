package com.example.arsipbpkpad.presentation.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.presentation.components.BottomNavItem
import com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.presentation.home.HomeUiState
import com.example.arsipbpkpad.presentation.home.HomeViewModel
import com.example.arsipbpkpad.presentation.home.component.HeaderSection
import com.example.arsipbpkpad.presentation.home.component.PrimaryStatCard
import com.example.arsipbpkpad.presentation.home.component.RecentArchiveTable
import com.example.arsipbpkpad.presentation.home.component.SecondaryStatCard
import com.example.arsipbpkpad.presentation.home.component.SectionHeader

@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToArchiveList: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onNavigateToScan = onNavigateToScan,
        onNavigateToArchiveList = onNavigateToArchiveList,
        onNavigateToDetail = onNavigateToDetail
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onNavigateToScan: () -> Unit,
    onNavigateToArchiveList: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    Scaffold(
        topBar = {
            BpkpadTopAppBar()
        },
        bottomBar = {
            BpkpadBottomNavigation(
                currentRoute = BottomNavItem.HOME.route,
                onNavigate = { item ->
                    when (item) {
                        BottomNavItem.HOME -> { /* Already here */ }
                        BottomNavItem.ARCHIVE -> onNavigateToArchiveList()
                        BottomNavItem.ADD -> onNavigateToScan()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_document),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderSection() }

            item {
                PrimaryStatCard(
                    title = stringResource(R.string.total_documents),
                    count = uiState.totalDocuments,
                    icon = Icons.Default.Done,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }

            item {
                PrimaryStatCard(
                    title = stringResource(R.string.expired_documents),
                    count = uiState.expiredDocuments,
                    icon = Icons.Default.Warning,
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            }

            // Grid Layout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryStatCard(
                        modifier = Modifier.weight(1f),
                        count = uiState.sp2dCount,
                        label = stringResource(R.string.type_sp2d),
                        icon = Icons.Default.Add
                    )
                    SecondaryStatCard(
                        modifier = Modifier.weight(1f),
                        count = uiState.spmCount,
                        label = stringResource(R.string.type_spm),
                        icon = Icons.Default.Done
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryStatCard(
                        modifier = Modifier.weight(1f),
                        count = uiState.sp3bCount,
                        label = stringResource(R.string.type_sp3b),
                        icon = Icons.Default.Warning
                    )
                    SecondaryStatCard(
                        modifier = Modifier.weight(1f),
                        count = uiState.dsbCount,
                        label = stringResource(R.string.type_dsb),
                        icon = Icons.Default.Build
                    )
                }
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.recently_added),
                    actionText = stringResource(R.string.view_all),
                    onActionClick = onNavigateToArchiveList
                )
            }

            item {
                RecentArchiveTable(
                    items = uiState.recentItems,
                    onArchiveClick = onNavigateToDetail
                )
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }
}
