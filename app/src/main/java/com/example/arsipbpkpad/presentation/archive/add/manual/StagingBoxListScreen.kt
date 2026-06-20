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
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StagingBoxListScreen(
    viewModel: RapidInputViewModel,
    onNavigateToRapidInput: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToBottomNav: (com.example.arsipbpkpad.presentation.components.BottomNavItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddBoxDialog by remember { mutableStateOf(false) }
    var boxToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { sessionId ->
            showAddBoxDialog = false 
            onNavigateToRapidInput(sessionId)
        }
    }

    LaunchedEffect(uiState.isUploadSuccess, uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar("Error: ${uiState.error}")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BpkpadTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                            text = "Staging Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            if (!uiState.isLoading && uiState.existingStagedBoxes.isNotEmpty()) {
                                viewModel.onEvent(RapidInputUiEvent.OnConfirmAllUpload)
                            }
                        },
                        enabled = !uiState.isLoading && uiState.existingStagedBoxes.isNotEmpty()
                    ) {
                        Text(
                            text = if (uiState.isLoading) "Processing..." else "Push All to\nDatabase",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.existingStagedBoxes.isNotEmpty() && !uiState.isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            textAlign = TextAlign.End
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(RapidInputUiEvent.TriggerSync) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Drafts",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                containerColor = Color.Transparent
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    viewModel.onEvent(RapidInputUiEvent.CreateNewSession)
                    showAddBoxDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Box")
            }
        },
        bottomBar = {
            com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation(
                currentRoute = com.example.arsipbpkpad.presentation.components.BottomNavItem.ADD.route,
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
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.existingStagedBoxes) { box ->
                            DashboardStagedBoxCard(
                                box = box,
                                onContinue = { 
                                    viewModel.onEvent(RapidInputUiEvent.SetCurrentSession(box.sessionId))
                                    onNavigateToRapidInput(box.sessionId) 
                                },
                                onDelete = { boxToDelete = box.sessionId }
                            )
                        }
                    }

                    DashboardSummary(uiState = uiState)
                }
            }

            if (boxToDelete != null) {
                AlertDialog(
                    onDismissRequest = { boxToDelete = null },
                    title = { Text("Hapus Staging Box") },
                    text = { Text("Apakah Anda yakin ingin menghapus box ini dari staging? Data dokumen di dalamnya akan hilang.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                boxToDelete?.let { viewModel.onEvent(RapidInputUiEvent.OnDeleteBoxSession(it)) }
                                boxToDelete = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { boxToDelete = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }

    // Task 1 Dialogs
    uiState.successMessage?.let { msg ->
        StatusDialog(
            title = "Berhasil",
            message = msg,
            onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissSuccess) },
            isSuccess = true
        )
    }

    uiState.error?.let { msg ->
        StatusDialog(
            title = "Kesalahan",
            message = msg,
            onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissError) },
            isSuccess = false
        )
    }

    uiState.warningMessage?.let { msg ->
        StatusDialog(
            title = "Peringatan",
            message = msg,
            onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissWarning) },
            isSuccess = null
        )
    }

    if (showAddBoxDialog) {
        AddBoxDialog(
            uiState = uiState,
            onDismiss = { showAddBoxDialog = false },
            onConfirm = {
                viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
            },
            onWarehouseChange = { viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange(it)) },
            onRackChange = { viewModel.onEvent(RapidInputUiEvent.OnRackChange(it)) },
            onBoxChange = { viewModel.onEvent(RapidInputUiEvent.OnBoxChange(it)) },
            onYearChange = { viewModel.onEvent(RapidInputUiEvent.OnYearChange(it)) }
        )
    }
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
                    text = "Box ${box.box}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Waiting for upload • ${box.itemCount} documents",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "${box.warehouse} - Rak ${box.rack} (${box.year})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DashboardSummary(uiState: RapidInputUiState) {
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
                        text = "$totalItems items in staging •",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$filledBoxes / ${uiState.existingStagedBoxes.size} Box Terisi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Green Progress Bar from image
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
            text = "Belum Ada Box di Staging",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gunakan tombol + untuk membuat box baru dan mulai memasukkan data arsip secara masal.",
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
    onWarehouseChange: (String) -> Unit,
    onRackChange: (String) -> Unit,
    onBoxChange: (String) -> Unit,
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
                    text = "Inisialisasi Box Baru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                    FormTextField(
                        label = "Gudang",
                        value = uiState.boxContext.warehouse,
                        onValueChange = onWarehouseChange,
                        placeholder = "Contoh: Gedung A / Lantai 2",
                        error = uiState.validationErrors["warehouse"]
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormTextField(
                            label = "Rak",
                            value = uiState.boxContext.rack,
                            onValueChange = onRackChange,
                            modifier = Modifier.weight(1f),
                            placeholder = "Contoh: R-01",
                            error = uiState.validationErrors["rack"]
                        )
                        FormTextField(
                            label = "Nomor Box",
                            value = uiState.boxContext.box,
                            onValueChange = onBoxChange,
                            modifier = Modifier.weight(1f),
                            placeholder = "Contoh: B-101",
                            error = uiState.validationErrors["box"]
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val years = remember { (1990..currentYear).reversed().map { it.toString() } }
                    FormDropdownField(
                        label = "Tahun Dokumen",
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
                        Text("Batal")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Buat & Input")
                    }
                }
            }
        }
    }
}
