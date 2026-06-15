package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.utils.CurrencyVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RapidInputScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScan: () -> Unit,
    viewModel: RapidInputViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Ensure the session observation starts as soon as we have a sessionId from route
    LaunchedEffect(sessionId) {
        if (sessionId.isNotEmpty()) {
            viewModel.onEvent(RapidInputUiEvent.SetCurrentSession(sessionId))
        }
    }

    if (uiState.showDuplicateWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(RapidInputUiEvent.DismissDuplicateWarning) },
            title = { Text("Peringatan Duplikasi") },
            text = { Text("Nomor dokumen '${uiState.documentNumber}' sudah terdaftar di sistem. Tetap simpan sebagai status '${uiState.copyStatus}' yang berbeda?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick(forceSave = true)) }
                ) {
                    Text("Tetap Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(RapidInputUiEvent.DismissDuplicateWarning) }) {
                    Text("Batal")
                }
            }
        )
    }

    LaunchedEffect(uiState.isUploadSuccess) {
        if (uiState.isUploadSuccess) {
            onNavigateBack()
            viewModel.onEvent(RapidInputUiEvent.ResetState)
        }
    }

    Scaffold(
        topBar = {
            BpkpadTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Input Dokumen Box ${uiState.boxContext.box}", 
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
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Scan Document")
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.stagedDocuments.isNotEmpty()) {
                        Button(
                            onClick = { 
                                uiState.currentSessionId?.let { 
                                    viewModel.onEvent(RapidInputUiEvent.OnConfirmUpload(it)) 
                                }
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Upload (${uiState.stagedDocuments.size}) Dokumen ke Database", 
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    OutlinedButton(
                        onClick = { onNavigateBack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32))
                    ) {
                        Text(
                            text = if (uiState.stagedDocuments.isEmpty()) "Selesai & Kembali" else "Batal & Keluar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF3FAFF)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- TOP SECTION: FORM ---
            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (uiState.editingId != null) "Edit Dokumen" else "Tambah Dokumen Baru",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FormDropdownField(
                                label = "Tipe",
                                value = uiState.docType,
                                options = listOf("SP2D", "SPM", "SP3B", "DSB"),
                                onOptionSelected = { viewModel.onEvent(RapidInputUiEvent.OnDocTypeChange(it)) },
                                modifier = Modifier.weight(1f)
                            )
                            FormDropdownField(
                                label = "Status",
                                value = uiState.copyStatus,
                                options = listOf("ORIGINAL", "COPY"),
                                onOptionSelected = { viewModel.onEvent(RapidInputUiEvent.OnCopyStatusChange(it)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        FormTextField(
                            label = "Nomor Dokumen",
                            value = uiState.documentNumber,
                            onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange(it)) },
                            error = uiState.validationErrors["docNumber"]
                        )
                        
                        FormTextField(
                            label = "Deskripsi / Perihal",
                            value = uiState.subject,
                            onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnSubjectChange(it)) },
                            singleLine = false,
                            minLines = 2,
                            error = uiState.validationErrors["subject"]
                        )
                        
                        FormTextField(
                            label = "Nominal (Opsional)",
                            value = uiState.nominal,
                            onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnNominalChange(it)) },
                            visualTransformation = CurrencyVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.editingId != null) "Perbarui di Staging" else "Tambah ke Staging",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        uiState.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // --- SECTION HEADER ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Staging (${uiState.stagedDocuments.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    if (uiState.stagedDocuments.isNotEmpty()) {
                        Text(
                            text = "Geser untuk aksi",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            items(uiState.stagedDocuments, key = { it.id }) { doc ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    StagedItemRow(
                        doc = doc,
                        onDelete = { viewModel.onEvent(RapidInputUiEvent.OnDeleteStagedDoc(doc.id)) },
                        onEdit = { viewModel.onEvent(RapidInputUiEvent.OnEditStagedDoc(doc)) }
                    )
                }
            }
            
            if (uiState.stagedDocuments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null, 
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Keranjang staging masih kosong.\nSilakan masukkan dokumen di atas.", 
                                color = Color.Gray, 
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StagedItemRow(
    doc: ArchiveDocument,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        ListItem(
            headlineContent = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = doc.type.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(doc.documentNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            supportingContent = { 
                Column {
                    Text(doc.thirdParty ?: "-", maxLines = 1, fontSize = 12.sp, color = Color.DarkGray)
                    Text(
                        text = "Status: ${doc.copyStatus} | Nominal: Rp ${doc.nominal?.toLong() ?: 0}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
