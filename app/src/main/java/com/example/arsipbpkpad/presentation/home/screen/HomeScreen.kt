package com.example.arsipbpkpad.presentation.home.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.UserRole
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
    onNavigateToArchiveList: (Int?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToStagingBoxList: () -> Unit,
    onNavigateToBoxManagement: () -> Unit,
    onNavigateToRapidInput: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToScan: () -> Unit,
    onLogout: () -> Unit,
    userRole: UserRole = UserRole.UNKNOWN,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        userRole = userRole,
        onNavigateToArchiveList = onNavigateToArchiveList,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToStagingBoxList = onNavigateToStagingBoxList,
        onNavigateToBoxManagement = onNavigateToBoxManagement,
        onNavigateToRapidInput = onNavigateToRapidInput,
        onNavigateToAnalytics = onNavigateToAnalytics,
        onNavigateToScan = onNavigateToScan,
        onLogout = onLogout
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    userRole: UserRole,
    onNavigateToArchiveList: (Int?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToStagingBoxList: () -> Unit,
    onNavigateToBoxManagement: () -> Unit,
    onNavigateToRapidInput: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToScan: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = { 
            BpkpadTopAppBar(
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            ) 
        },
        bottomBar = {
            HomeBottomNavigation(
                userRole = userRole,
                onNavigateToArchiveList = onNavigateToArchiveList,
                onNavigateToStagingBoxList = onNavigateToStagingBoxList,
                onNavigateToBoxManagement = onNavigateToBoxManagement,
                onNavigateToAnalytics = onNavigateToAnalytics
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        HomeMainList(
            uiState = uiState,
            userRole = userRole,
            paddingValues = paddingValues,
            onNavigateToArchiveList = onNavigateToArchiveList,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToStagingBoxList = onNavigateToStagingBoxList
        )
    }
}

@Composable
fun HomeBottomNavigation(
    userRole: UserRole,
    onNavigateToArchiveList: (Int?) -> Unit,
    onNavigateToStagingBoxList: () -> Unit,
    onNavigateToBoxManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    BpkpadBottomNavigation(
        currentRoute = BottomNavItem.HOME.route,
        userRole = userRole,
        onNavigate = { item ->
            when (item) {
                BottomNavItem.HOME -> { /* Already here */ }
                BottomNavItem.ARCHIVE -> onNavigateToArchiveList(null)
                BottomNavItem.ADD -> onNavigateToStagingBoxList()
                BottomNavItem.STORAGE -> onNavigateToBoxManagement()
                BottomNavItem.ANALYTICS -> onNavigateToAnalytics()
            }
        }
    )
}

@Composable
fun HomeMainList(
    uiState: HomeUiState,
    userRole: UserRole,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onNavigateToArchiveList: (Int?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToStagingBoxList: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 88.dp,
            start = 20.dp,
            end = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeaderSection() }

        if (userRole == UserRole.ARSIPARIS && uiState.activeStagingBoxes.isNotEmpty()) {
            item {
                val totalDocs = uiState.activeStagingBoxes.sumOf { it.itemCount }
                val totalBoxes = uiState.activeStagingBoxes.size
                StagingStatusCard(
                    count = totalDocs,
                    summary = stringResource(R.string.staging_boxes_ready, totalBoxes),
                    onClick = onNavigateToStagingBoxList,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        item { HomePrimaryStats(uiState) }
        item { HomeSecondaryStats(uiState) }

        item {
            SectionHeader(
                title = stringResource(R.string.recently_added),
                actionText = stringResource(R.string.view_all),
                onActionClick = { onNavigateToArchiveList(null) }
            )
        }

        item {
            RecentArchiveTable(
                items = uiState.recentItems,
                onArchiveClick = onNavigateToDetail
            )
        }
    }
}

@Composable
fun HomePrimaryStats(uiState: HomeUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PrimaryStatCard(
            title = stringResource(R.string.total_documents),
            count = uiState.totalDocuments,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            contentColor = MaterialTheme.colorScheme.primary
        )
        PrimaryStatCard(
            title = stringResource(R.string.expired_documents),
            count = uiState.expiredDocuments,
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun HomeSecondaryStats(uiState: HomeUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryStatCard(
                modifier = Modifier.weight(1f),
                count = uiState.sppCount,
                label = stringResource(R.string.type_spp)
            )
            SecondaryStatCard(
                modifier = Modifier.weight(1f),
                count = uiState.spmCount,
                label = stringResource(R.string.type_spm)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryStatCard(
                modifier = Modifier.weight(1f),
                count = uiState.sp2dCount,
                label = stringResource(R.string.type_sp2d)
            )
            SecondaryStatCard(
                modifier = Modifier.weight(1f),
                count = uiState.spjCount,
                label = stringResource(R.string.type_spj)
            )
        }
    }
}

@Composable
fun StagingStatusCard(
    count: Int,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            StagingCardHeader()
            Spacer(modifier = Modifier.height(16.dp))
            StagingCardContent(count = count, summary = summary)
            Spacer(modifier = Modifier.height(16.dp))
            StagingProgressBar()
        }
    }
}

@Composable
fun StagingCardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.btn_sync),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.staging_status_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun StagingCardContent(count: Int, summary: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = stringResource(R.string.staging_docs_waiting, count),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            // Summary originally contained the year, but we keep it generic or rely on string resources
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StagingProgressBar() {
    LinearProgressIndicator(
        progress = { 1f },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant
    )
}
