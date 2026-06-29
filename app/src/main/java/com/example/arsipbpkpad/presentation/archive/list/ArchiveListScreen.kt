package com.example.arsipbpkpad.presentation.archive.list

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.UserRole
import com.example.arsipbpkpad.domain.model.canExport
import com.example.arsipbpkpad.domain.model.canMutateArchive
import com.example.arsipbpkpad.presentation.components.ArchiveListItemCard
import com.example.arsipbpkpad.presentation.components.ArchiveTableHeader
import com.example.arsipbpkpad.presentation.components.BottomNavItem
import com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation
import com.example.arsipbpkpad.presentation.components.BpkpadConfirmDialog
import com.example.arsipbpkpad.presentation.components.BpkpadExpandableFAB
import com.example.arsipbpkpad.presentation.components.StatusDialog
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme
import com.example.arsipbpkpad.utils.DateUtils

@Composable
fun ArchiveListScreen(
    year: Int? = null,
    userRole: UserRole = UserRole.UNKNOWN,
    viewModel: ArchiveListViewModel = hiltViewModel(),
    stagingViewModel: com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRapidInput: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stagingState by stagingViewModel.uiState.collectAsStateWithLifecycle()
    val archives by viewModel.archivesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val context = LocalContext.current
    val resources = context.resources
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Intercept system back button to go back to year selection if a filter is confirmed
    BackHandler(enabled = uiState.isFilterConfirmed || uiState.isSelectionMode) {
        if (uiState.isSelectionMode) {
            viewModel.onEvent(ArchiveListUiEvent.ToggleSelectionMode())
        } else {
            viewModel.onEvent(ArchiveListUiEvent.OnResetFilter)
        }
    }

    var showImportConfirm by remember { mutableStateOf(false) }
    var showExportConfirm by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingImportUri = it
            showImportConfirm = true
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                viewModel.onEvent(ArchiveListUiEvent.ExportExcel(outputStream))
            }
        }
    }

    LaunchedEffect(uiState.excelOperationMessage) {
        uiState.excelOperationMessage?.let { message ->
            val resolvedMessage = when {
                message == "IMPORT_SUCCESS_SYNCED" -> resources.getString(R.string.msg_import_success_synced)
                message == "IMPORT_SUCCESS_LOCAL" -> resources.getString(R.string.msg_import_success_local)
                message == "EXPORT_SUCCESS" -> resources.getString(R.string.msg_export_success)
                message.startsWith("ERROR_IMPORT:") -> {
                    val detail = message.removePrefix("ERROR_IMPORT:")
                    resources.getString(R.string.msg_import_failed, detail)
                }
                message.startsWith("ERROR_EXPORT:") -> {
                    val detail = message.removePrefix("ERROR_EXPORT:")
                    resources.getString(R.string.msg_export_failed, detail)
                }
                else -> message
            }
            
            if (message.startsWith("IMPORT_SUCCESS") || message == "EXPORT_SUCCESS") {
                showSuccessDialog = resolvedMessage
            } else {
                snackbarHostState.showSnackbar(resolvedMessage)
            }
        }
    }

    LaunchedEffect(year) {
        if (year != null) {
            viewModel.updateInitialYear(year)
        }
    }

    LaunchedEffect(stagingState.isBoxContextSet) {
        if (stagingState.isBoxContextSet) {
            onNavigateToRapidInput()
        }
    }

    if (showImportConfirm) {
        BpkpadConfirmDialog(
            title = stringResource(R.string.title_confirm_import),
            message = stringResource(R.string.msg_confirm_import),
            confirmText = stringResource(R.string.btn_import),
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = {
                pendingImportUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.let { inputStream ->
                        viewModel.onEvent(ArchiveListUiEvent.ImportExcel(inputStream))
                    }
                }
                showImportConfirm = false
            },
            onDismiss = { showImportConfirm = false }
        )
    }

    if (showExportConfirm) {
        BpkpadConfirmDialog(
            title = stringResource(R.string.title_confirm_export),
            message = stringResource(R.string.msg_confirm_export),
            confirmText = stringResource(R.string.btn_export),
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = {
                exportLauncher.launch("Arsip_${System.currentTimeMillis()}.xlsx")
                showExportConfirm = false
            },
            onDismiss = { showExportConfirm = false }
        )
    }

    if (uiState.showDeleteConfirmDialog) {
        BpkpadConfirmDialog(
            title = stringResource(R.string.title_delete_selected_archives),
            message = stringResource(R.string.msg_delete_selected_archives, uiState.selectedArchiveIds.size),
            confirmText = stringResource(R.string.btn_delete),
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = { viewModel.onEvent(ArchiveListUiEvent.ConfirmDeleteSelected) },
            onDismiss = { viewModel.onEvent(ArchiveListUiEvent.DismissDeleteConfirm) },
            type = com.example.arsipbpkpad.presentation.components.DialogType.DESTRUCTIVE
        )
    }

    uiState.successMessage?.let { msg ->
        StatusDialog(
            title = stringResource(R.string.title_success),
            message = msg,
            onDismiss = { viewModel.onEvent(ArchiveListUiEvent.DismissSuccess) },
            isSuccess = true
        )
    }

    uiState.errorMessage?.let { msg ->
        StatusDialog(
            title = stringResource(R.string.title_error),
            message = msg,
            onDismiss = { viewModel.onEvent(ArchiveListUiEvent.DismissError) },
            isSuccess = false
        )
    }

    showSuccessDialog?.let { msg ->
        StatusDialog(
            title = stringResource(R.string.title_success),
            message = msg,
            onDismiss = { showSuccessDialog = null },
            isSuccess = true
        )
    }

    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedArchiveIds.size,
                    onClearSelection = { viewModel.onEvent(ArchiveListUiEvent.ToggleSelectionMode()) }
                )
            } else {
                ArchiveListTopBar(
                    isFilterConfirmed = uiState.isFilterConfirmed,
                    onBackClick = {
                        if (uiState.isFilterConfirmed) {
                            viewModel.onEvent(ArchiveListUiEvent.OnResetFilter)
                        } else {
                            onNavigateBack()
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BpkpadBottomNavigation(
                currentRoute = BottomNavItem.ARCHIVE.route,
                userRole = userRole,
                onNavigate = onNavigateToBottomNav
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            if (uiState.isSelectionMode) {
                SelectionFABs(
                    selectedCount = uiState.selectedArchiveIds.size,
                    onClearSelection = { viewModel.onEvent(ArchiveListUiEvent.ToggleSelectionMode()) },
                    onSelectAll = { 
                        viewModel.onEvent(ArchiveListUiEvent.SelectAllArchives(archives.map { it.id })) 
                    },
                    onDeleteSelected = { viewModel.onEvent(ArchiveListUiEvent.RequestDeleteSelected) },
                    userRole = userRole
                )
            } else if (uiState.isFilterConfirmed && userRole.canMutateArchive()) {
                BpkpadExpandableFAB(
                    onManualInputClick = { 
                        onNavigateToBottomNav(BottomNavItem.ADD)
                    },
                    onOcrScanClick = onNavigateToScan
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.selectedYears.isEmpty() && !uiState.isFilterConfirmed) {
                YearSelectionGrid(
                    availableYears = uiState.availableYears,
                    yearStats = uiState.yearStats,
                    onYearClick = { viewModel.onEvent(ArchiveListUiEvent.OnYearToggle(it)) },
                    paddingValues = paddingValues
                )
            } else {
                ArchiveListContentOnly(
                    uiState = uiState,
                    archives = archives,
                    onSearchQueryChange = { query -> 
                        viewModel.onEvent(ArchiveListUiEvent.OnSearchQueryChange(query)) 
                    },
                    onFilterChange = { type ->
                        viewModel.onEvent(ArchiveListUiEvent.OnFilterChange(type))
                    },
                    onArchiveClick = { id ->
                        if (uiState.isSelectionMode) {
                            viewModel.onEvent(ArchiveListUiEvent.ToggleArchiveSelection(id))
                        } else {
                            onNavigateToDetail(id)
                        }
                    },
                    onArchiveLongClick = { id ->
                        if (!uiState.isSelectionMode && userRole.canMutateArchive()) {
                            viewModel.onEvent(ArchiveListUiEvent.ToggleSelectionMode(id))
                        }
                    },
                    isSelectionMode = uiState.isSelectionMode,
                    selectedArchiveIds = uiState.selectedArchiveIds,
                    onImportClick = {
                        importLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    },
                    onExportClick = {
                        if (archives.isNotEmpty()) {
                            showExportConfirm = true
                        }
                    },
                    isExportEnabled = archives.isNotEmpty(),
                    userRole = userRole,
                    paddingValues = paddingValues
                )
            }
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).padding(paddingValues))
            }
        }
    }
}

@Composable
fun ArchiveListTopBar(
    isFilterConfirmed: Boolean,
    onBackClick: () -> Unit
) {
    com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar(
        title = {
            Text(
                text = if (isFilterConfirmed) stringResource(R.string.archive_list_title) else stringResource(R.string.archival_repository_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit
) {
    com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.label_selected_count, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.btn_clear_selection),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
fun SelectionFABs(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    userRole: UserRole = UserRole.UNKNOWN
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Select All FAB
        ExtendedFloatingActionButton(
            onClick = onSelectAll,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Default.SelectAll, contentDescription = null) },
            text = { Text(stringResource(R.string.btn_select_all)) }
        )

        // Cancel FAB
        ExtendedFloatingActionButton(
            onClick = onClearSelection,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Default.Close, contentDescription = null) },
            text = { Text(stringResource(R.string.btn_clear_selection)) }
        )

        // Delete/Confirm FAB
        if (userRole.canMutateArchive()) {
            ExtendedFloatingActionButton(
                onClick = onDeleteSelected,
                expanded = true,
                containerColor = if (selectedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                contentColor = if (selectedCount > 0) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                text = { Text(stringResource(R.string.btn_confirm)) }
            )
        }
    }
}

@Composable
fun YearSelectionGrid(
    availableYears: List<Int>,
    yearStats: List<com.example.arsipbpkpad.domain.model.YearStats>,
    onYearClick: (Int) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding() + 24.dp))
        ArchivalRepositoryHeader()
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            availableYears.chunked(2).forEach { rowYears ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowYears.forEach { y ->
                            val stats = yearStats.find { it.year == y }
                            YearGridCard(
                                year = y,
                                recordsCount = stats?.count ?: 0,
                                lastUpdated = DateUtils.formatDateTime(stats?.lastUpdated),
                                onClick = { onYearClick(y) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowYears.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ArchivalRepositoryHeader() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val cardHeight = if (isLandscape) 120.dp else 180.dp
    val titleStyle = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall
    val subtitleStyle = if (isLandscape) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium
    val padding = if (isLandscape) 16.dp else 24.dp
    val spacerHeight = if (isLandscape) 4.dp else 12.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(if (isLandscape) 24.dp else 40.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLandscape) stringResource(R.string.archival_repository_title) 
                       else stringResource(R.string.archival_repository_title).replace(" ", "\n"),
                style = titleStyle,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
                lineHeight = if (isLandscape) 28.sp else 36.sp
            )
            Spacer(modifier = Modifier.height(spacerHeight))
            Text(
                text = stringResource(R.string.archival_repository_subtitle),
                style = subtitleStyle,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun YearGridCard(
    year: Int,
    recordsCount: Int,
    lastUpdated: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.label_records, recordsCount),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.label_updated, lastUpdated),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ArchiveListContentOnly(
    uiState: ArchiveListUiState,
    archives: List<ArchiveDocument>,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
    onArchiveLongClick: (String) -> Unit = {},
    isSelectionMode: Boolean = false,
    selectedArchiveIds: Set<String> = emptySet(),
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    isExportEnabled: Boolean = true,
    userRole: UserRole = UserRole.UNKNOWN,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding() + 8.dp))
            ActiveFilterSummary(selectedYears = uiState.selectedYears.sortedDescending())
            Spacer(modifier = Modifier.height(12.dp))
            ArchiveSearchBar(query = uiState.searchQuery, onQueryChange = onSearchQueryChange)
            Spacer(modifier = Modifier.height(12.dp))
            DocTypeFilterRow(selectedFilter = uiState.selectedFilter, onFilterChange = onFilterChange)
            Spacer(modifier = Modifier.height(12.dp))
            ExcelActionButtons(
                onImportClick = onImportClick,
                onExportClick = onExportClick,
                isExportEnabled = isExportEnabled,
                userRole = userRole
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        ArchiveResultList(
            archives = archives,
            onArchiveClick = onArchiveClick,
            onArchiveLongClick = onArchiveLongClick,
            isSelectionMode = isSelectionMode,
            selectedArchiveIds = selectedArchiveIds,
            paddingValues = paddingValues
        )
    }
}

@Composable
fun ExcelActionButtons(
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    isExportEnabled: Boolean = true,
    userRole: UserRole = UserRole.UNKNOWN
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (userRole.canMutateArchive()) {
            OutlinedButton(
                onClick = onImportClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onExportClick,
            enabled = isExportEnabled && userRole.canExport(),
            modifier = if (userRole.canMutateArchive()) Modifier.weight(1f) else Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActiveFilterSummary(selectedYears: List<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_year_label, selectedYears.joinToString(", ")),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ArchiveSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(R.string.search_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true
    )
}

@Composable
fun DocTypeFilterRow(selectedFilter: String, onFilterChange: (String) -> Unit) {
    val allFilter = stringResource(R.string.filter_all)
    val sp2dFilter = stringResource(R.string.type_sp2d)
    val spmFilter = stringResource(R.string.type_spm)
    val sppFilter = stringResource(R.string.type_spp)
    val spjFilter = stringResource(R.string.type_spj)
    
    val filters = remember(allFilter, sp2dFilter, spmFilter, sppFilter, spjFilter) {
        listOf(allFilter, sp2dFilter, spmFilter, sppFilter, spjFilter)
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter || (filter == allFilter && selectedFilter == "Semua")
            FilterChip(
                selected = isSelected,
                onClick = { 
                    if (filter == allFilter) onFilterChange("Semua") else onFilterChange(filter) 
                },
                label = { Text(filter) },
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun ArchiveResultList(
    archives: List<ArchiveDocument>,
    onArchiveClick: (String) -> Unit,
    onArchiveLongClick: (String) -> Unit = {},
    isSelectionMode: Boolean = false,
    selectedArchiveIds: Set<String> = emptySet(),
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val scrollState = rememberScrollState()
    
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minTableWidth = (if (isSelectionMode) 48.dp else 0.dp) + 632.dp // Total of min widths from ArchiveTableHeader
        val tableWidth = maxOf(maxWidth, minTableWidth)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            Column(modifier = Modifier.width(tableWidth)) {
                ArchiveTableHeader(isSelectionMode = isSelectionMode)
                
                if (archives.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            bottom = paddingValues.calculateBottomPadding() + 88.dp
                        ),
                    ) {
                        items(archives.size) { index ->
                            val archive = archives[index]
                            ArchiveListItemCard(
                                no = index + 1,
                                archive = archive,
                                onClick = { onArchiveClick(archive.id) },
                                onLongClick = { onArchiveLongClick(archive.id) },
                                isSelected = selectedArchiveIds.contains(archive.id),
                                isSelectionMode = isSelectionMode
                            )
                        }
                    }
                }
            }
        }

        if (archives.isEmpty()) {
            EmptyArchiveSearchResults(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            )
        }
    }
}

@Composable
fun EmptyArchiveSearchResults(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info, 
            contentDescription = null, 
            modifier = Modifier.size(64.dp), 
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_docs_found),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun ArchiveListScreenPreview() {
    ArsipBPKPADTheme {
        ArchiveListContentOnly(
            uiState = ArchiveListUiState(
                isFilterConfirmed = true,
                selectedYears = setOf(2014),
                selectedFilter = "SP2D"
            ),
            archives = listOf(
                ArchiveDocument(
                    id = "1",
                    type = "SP2D",
                    documentNumber = "SP2D-1029",
                    classificationCode = "900.1.3.1",
                    copyType = com.example.arsipbpkpad.domain.model.DocCopyType.ORIGINAL,
                    copyCount = 1,
                    nominal = 1000000.0,
                    description = "Pembayaran ATK",
                    year = 2014,
                    status = DocStatus.AVAILABLE,
                    idStorageLocation = "LOC1",
                    metadata = null,
                    createdBy = null,
                    verifiedBy = null,
                    createdAt = "2014-05-10",
                    updatedAt = null
                )
            ),
            onSearchQueryChange = {},
            onFilterChange = {},
            onArchiveClick = {},
            onImportClick = {},
            onExportClick = {},
            userRole = UserRole.ARSIPARIS
        )
    }
}
