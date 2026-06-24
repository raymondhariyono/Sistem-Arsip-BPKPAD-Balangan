package com.example.arsipbpkpad.presentation.archive.add.manual

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.StagedBox
import com.example.arsipbpkpad.presentation.components.BottomNavItem
import com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.presentation.components.FormDropdownField
import com.example.arsipbpkpad.presentation.components.HierarchicalLocationSelector
import com.example.arsipbpkpad.presentation.components.StatusDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StagingBoxListScreen(
    viewModel: RapidInputViewModel,
    userRole: com.example.arsipbpkpad.domain.model.UserRole = com.example.arsipbpkpad.domain.model.UserRole.UNKNOWN,
    onNavigateToRapidInput: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddBoxDialog by remember { mutableStateOf(false) }
    var boxToDelete by remember { mutableStateOf<String?>(null) }
    var showUploadConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { sessionId ->
            showAddBoxDialog = false 
            onNavigateToRapidInput(sessionId)
        }
    }

    val errorTitle = stringResource(R.string.title_error)
    val successTitle = stringResource(R.string.title_success)
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StagingBoxListTopBar(
                isLoading = uiState.isLoading,
                onBackClick = onNavigateBack,
                onSyncClick = { viewModel.onEvent(RapidInputUiEvent.TriggerSync) }
            )
        },
        floatingActionButton = {
            AddBoxFAB(onClick = { 
                viewModel.onEvent(RapidInputUiEvent.CreateNewSession)
                showAddBoxDialog = true 
            })
        },
        bottomBar = {
            BpkpadBottomNavigation(
                currentRoute = BottomNavItem.ADD.route,
                userRole = userRole,
                onNavigate = onNavigateToBottomNav
            )
        },
        containerColor = MaterialTheme.colorScheme.surface 
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.onEvent(RapidInputUiEvent.TriggerSync) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (uiState.existingStagedBoxes.isEmpty()) {
                EmptyStagingContent()
            } else {
                StagingBoxListContent(
                    boxes = uiState.existingStagedBoxes,
                    onBoxClick = { 
                        viewModel.onEvent(RapidInputUiEvent.SetCurrentSession(it))
                        onNavigateToRapidInput(it) 
                    },
                    onBoxDelete = { boxToDelete = it },
                    onPushAllClick = { showUploadConfirm = true },
                    uiState = uiState
                )
            }

            if (boxToDelete != null) {
                DeleteBoxDialog(
                    onConfirm = {
                        boxToDelete?.let { viewModel.onEvent(RapidInputUiEvent.OnDeleteBoxSession(it)) }
                        boxToDelete = null
                    },
                    onDismiss = { boxToDelete = null }
                )
            }

            if (showUploadConfirm) {
                AlertDialog(
                    onDismissRequest = { showUploadConfirm = false },
                    title = { Text(text = "Konfirmasi Unggah") },
                    text = { Text(text = "Apakah Anda yakin ingin mengunggah semua dokumen dari penyimpanan sementara ke penyimpanan permanen?") },
                    confirmButton = {
                        Button(onClick = {
                            showUploadConfirm = false
                            viewModel.onEvent(RapidInputUiEvent.OnConfirmAllUpload)
                        }) {
                            Text("Unggah")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUploadConfirm = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Common Dialogs
            uiState.successMessage?.let { msg ->
                StatusDialog(
                    title = successTitle,
                    message = msg.asString(),
                    onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissSuccess) },
                    isSuccess = true
                )
            }

            uiState.error?.let { msg ->
                StatusDialog(
                    title = errorTitle,
                    message = msg,
                    onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissError) },
                    isSuccess = false
                )
            }

            uiState.warningMessage?.let { msg ->
                StatusDialog(
                    title = stringResource(R.string.title_warning),
                    message = msg.asString(),
                    onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissWarning) },
                    isSuccess = null
                )
            }

            if (showAddBoxDialog) {
                AddBoxDialog(
                    uiState = uiState,
                    onDismiss = { showAddBoxDialog = false },
                    onConfirm = { viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext) },
                    onRoomChange = { viewModel.onEvent(RapidInputUiEvent.OnRoomChange(it)) },
                    onRoomSelected = { viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(it)) },
                    onCreateRoom = { viewModel.onEvent(RapidInputUiEvent.OnCreateRoom(it)) },
                    onShelfChange = { viewModel.onEvent(RapidInputUiEvent.OnShelfChange(it)) },
                    onShelfSelected = { viewModel.onEvent(RapidInputUiEvent.OnShelfSelected(it)) },
                    onCreateShelf = { viewModel.onEvent(RapidInputUiEvent.OnCreateShelf(it)) },
                    onBoxLocationChange = { viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange(it)) },
                    onYearChange = { viewModel.onEvent(RapidInputUiEvent.OnYearChange(it)) }
                )
            }
        }
    }
}

@Composable
fun StagingBoxListTopBar(
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    BpkpadTopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_balangan),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.staging_status_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            TextButton(
                onClick = onSyncClick,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.btn_sync),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = Color.Transparent
    )
}

@Composable
fun AddBoxFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Tambah Box")
    }
}

@Composable
fun StagingBoxListContent(
    boxes: List<StagedBox>,
    onBoxClick: (String) -> Unit,
    onBoxDelete: (String) -> Unit,
    onPushAllClick: () -> Unit,
    uiState: RapidInputUiState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(boxes) { box ->
                DashboardStagedBoxCard(
                    box = box,
                    onContinue = { onBoxClick(box.sessionId) },
                    onDelete = { onBoxDelete(box.sessionId) }
                )
            }
        }
        DashboardSummary(
            uiState = uiState,
            onPushAllClick = onPushAllClick
        )
    }
}

@Composable
fun DeleteBoxDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_delete_staging_box)) },
        text = { Text(stringResource(R.string.msg_delete_staging_box)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.btn_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun DashboardStagedBoxCard(
    box: StagedBox,
    onContinue: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContinue() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(32.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_box_number, box.box),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.label_waiting_upload, box.itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "${box.warehouse} - Rak ${box.rack}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DashboardSummary(
    uiState: RapidInputUiState,
    onPushAllClick: () -> Unit
) {
    val filledBoxes = uiState.existingStagedBoxes.count { it.itemCount > 0 }
    val totalItems = uiState.existingStagedBoxes.sumOf { it.itemCount }
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.label_items_in_staging, totalItems),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.label_boxes_filled, filledBoxes, uiState.existingStagedBoxes.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                val progress = if (uiState.existingStagedBoxes.isNotEmpty()) {
                    filledBoxes.toFloat() / uiState.existingStagedBoxes.size
                } else 0f

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onPushAllClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.existingStagedBoxes.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isLoading) {
                    Text(stringResource(R.string.btn_processing))
                } else {
                    Text(stringResource(R.string.btn_push_to_db))
                }
            }
        }
    }
}

@Composable
fun EmptyStagingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.empty_staging_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_staging_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddBoxDialog(
    uiState: RapidInputUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onRoomChange: (String) -> Unit,
    onRoomSelected: (com.example.arsipbpkpad.domain.model.Room?) -> Unit,
    onCreateRoom: (String) -> Unit,
    onShelfChange: (String) -> Unit,
    onShelfSelected: (com.example.arsipbpkpad.domain.model.Shelf?) -> Unit,
    onCreateShelf: (String) -> Unit,
    onBoxLocationChange: (String) -> Unit,
    onYearChange: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.title_init_new_box),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                HierarchicalLocationSelector(
                    roomsList = uiState.roomsList,
                    shelvesList = uiState.shelvesList,
                    selectedRoom = uiState.selectedRoom,
                    selectedShelf = uiState.selectedShelf,
                    typedRoom = uiState.typedRoom,
                    typedShelf = uiState.typedShelf,
                    typedBox = uiState.typedBox,
                    onRoomChange = onRoomChange,
                    onRoomSelected = onRoomSelected,
                    onCreateRoom = onCreateRoom,
                    onShelfChange = onShelfChange,
                    onShelfSelected = onShelfSelected,
                    onCreateShelf = onCreateShelf,
                    onBoxChange = onBoxLocationChange,
                    boxError = uiState.validationErrors["box"]
                )

                Spacer(modifier = Modifier.height(12.dp))

                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                val years = remember { (1990..currentYear).reversed().map { it.toString() } }
                FormDropdownField(
                    label = stringResource(R.string.label_doc_year),
                    value = uiState.boxContext.year,
                    options = years,
                    onOptionSelected = onYearChange,
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.validationErrors.containsKey("year")) {
                    Text(
                        text = uiState.validationErrors["year"] ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.btn_create_input))
                    }
                }
            }
        }
    }
}
