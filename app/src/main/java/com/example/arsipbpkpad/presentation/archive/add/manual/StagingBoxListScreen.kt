package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(uiState.isBoxContextSet, uiState.currentSessionId) {
        if (uiState.isBoxContextSet && uiState.currentSessionId != null) {
            onNavigateToRapidInput(uiState.currentSessionId!!)
            viewModel.onEvent(RapidInputUiEvent.OnHandledNavigation)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Staging Area (Box)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Profile side effect */ }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Box")
            }
        },
        containerColor = Color(0xFFF3FAFF)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.existingStagedBoxes.isEmpty()) {
                EmptyStagingContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Daftar Box dalam Staging",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.existingStagedBoxes) { box ->
                        StagedBoxCard(
                            box = box,
                            onContinue = { viewModel.onEvent(RapidInputUiEvent.SetCurrentSession(box.sessionId)) },
                            onUpload = { viewModel.onEvent(RapidInputUiEvent.OnConfirmUpload(box.sessionId)) },
                            onDelete = { boxToDelete = box.sessionId }
                        )
                    }
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
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
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
                // Dialog will be dismissed when navigation happens via LaunchedEffect
            },
            onWarehouseChange = { viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange(it)) },
            onRackChange = { viewModel.onEvent(RapidInputUiEvent.OnRackChange(it)) },
            onBoxChange = { viewModel.onEvent(RapidInputUiEvent.OnBoxChange(it)) },
            onYearChange = { viewModel.onEvent(RapidInputUiEvent.OnYearChange(it)) }
        )
    }
}

@Composable
fun StagedBoxCard(
    box: StagedBox,
    onContinue: () -> Unit,
    onUpload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Box ${box.box}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${box.warehouse} - Rak ${box.rack} (${box.year})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${box.itemCount} Item",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(0.4f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", fontSize = 12.sp)
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(0.6f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lanjutkan Input", fontSize = 12.sp)
                }

                Button(
                    onClick = onUpload,
                    modifier = Modifier.weight(0.6f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload", fontSize = 12.sp)
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
