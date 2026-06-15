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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.arsipbpkpad.presentation.archive.list.FormTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StagingBoxListScreen(
    viewModel: RapidInputViewModel,
    onNavigateToRapidInput: (String) -> Unit,
    onNavigateBack: () -> Unit
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
        if (uiState.isUploadSuccess) {
            snackbarHostState.showSnackbar("Berhasil diunggah ke database!")
            viewModel.onEvent(RapidInputUiEvent.ResetState)
        } else if (uiState.error != null) {
            snackbarHostState.showSnackbar("Error: ${uiState.error}")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color(0xFF1B5E20)
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Staging Status",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
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
                            color = if (uiState.existingStagedBoxes.isNotEmpty() && !uiState.isLoading) Color(0xFF1B5E20) else Color.Gray,
                            textAlign = TextAlign.End
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    viewModel.onEvent(RapidInputUiEvent.CreateNewSession)
                    showAddBoxDialog = true 
                },
                containerColor = Color(0xFF1B5E20),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Box")
            }
        },
        containerColor = Color(0xFFE1F5FE) 
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1B5E20))
                }
            }
        }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFA5D6A7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Box ${box.box}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                Text(
                    text = "Waiting for upload • ${box.itemCount} documents",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF546E7A)
                )
                Text(
                    text = "${box.warehouse} - Rak ${box.rack} (${box.year})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF78909C)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DashboardSummary(uiState: RapidInputUiState) {
    val totalItems = uiState.existingStagedBoxes.sumOf { it.itemCount }
    
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            HorizontalDivider(color = Color(0xFFCFD8DC), thickness = 1.dp)
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
                        color = Color(0xFF455A64),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${uiState.existingStagedBoxes.size} Boxes ready",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF78909C)
                    )
                }
                
                // Green Progress Bar from image
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(12.dp)
                        .background(Color(0xFF1B5E20), RoundedCornerShape(6.dp))
                )
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
                .background(Color(0xFFE8F5E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Belum Ada Box di Staging",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
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
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Inisialisasi Box Baru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                FormTextField(
                    label = "Gudang",
                    value = uiState.boxContext.warehouse,
                    onValueChange = onWarehouseChange,
                    error = uiState.validationErrors["warehouse"]
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        label = "Rak",
                        value = uiState.boxContext.rack,
                        onValueChange = onRackChange,
                        modifier = Modifier.weight(1f),
                        error = uiState.validationErrors["rack"]
                    )
                    FormTextField(
                        label = "Nomor Box",
                        value = uiState.boxContext.box,
                        onValueChange = onBoxChange,
                        modifier = Modifier.weight(1f),
                        error = uiState.validationErrors["box"]
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    label = "Tahun Dokumen",
                    value = uiState.boxContext.year,
                    onValueChange = onYearChange,
                    error = uiState.validationErrors["year"]
                )

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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Text("Buat & Input")
                    }
                }
            }
        }
    }
}
