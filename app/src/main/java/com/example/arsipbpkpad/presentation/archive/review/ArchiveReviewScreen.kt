package com.example.arsipbpkpad.presentation.archive.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme

@Composable
fun ArchiveReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: ArchiveReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArchiveReviewContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToHome = onNavigateToHome
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveReviewContent(
    uiState: ArchiveReviewUiState,
    onEvent: (ArchiveReviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                onEvent(ArchiveReviewUiEvent.DismissSuccessDialog)
                onNavigateToHome()
            },
            confirmButton = {
                Button(onClick = { 
                    onEvent(ArchiveReviewUiEvent.DismissSuccessDialog)
                    onNavigateToHome()
                }) {
                    Text("OK")
                }
            },
            title = { Text("Berhasil") },
            text = { Text("Semua arsip dalam box ini berhasil disimpan.") },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        )
    }

    Scaffold(
        topBar = { ReviewTopAppBar(onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Review Staging Box",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tinjau daftar dokumen sebelum melakukan verifikasi akhir untuk satu box ini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Location Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Lokasi Penyimpanan Box", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.warehouse,
                            onValueChange = { onEvent(ArchiveReviewUiEvent.OnWarehouseChange(it)) },
                            label = { Text("Gudang") },
                            modifier = Modifier.weight(1f),
                            isError = uiState.validationErrors.containsKey("warehouse")
                        )
                        OutlinedTextField(
                            value = uiState.rack,
                            onValueChange = { onEvent(ArchiveReviewUiEvent.OnRackChange(it)) },
                            label = { Text("Rak") },
                            modifier = Modifier.weight(0.7f),
                            isError = uiState.validationErrors.containsKey("rack")
                        )
                        OutlinedTextField(
                            value = uiState.box,
                            onValueChange = { onEvent(ArchiveReviewUiEvent.OnBoxChange(it)) },
                            label = { Text("Box") },
                            modifier = Modifier.weight(0.7f),
                            isError = uiState.validationErrors.containsKey("box")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Staged List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.stagedDocuments) { doc ->
                    StagedDocItem(
                        docNumber = doc.documentNumber,
                        type = doc.type.name,
                        copyStatus = doc.copyStatus.name,
                        onDelete = { onEvent(ArchiveReviewUiEvent.OnDeleteStagedDoc(doc.id)) }
                    )
                }
                
                if (uiState.stagedDocuments.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada dokumen di staging area", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onEvent(ArchiveReviewUiEvent.OnApplyClick) },
                enabled = uiState.stagedDocuments.isNotEmpty() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Apply / Verifikasi 1 Box (${uiState.stagedDocuments.size} Dokumen)", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StagedDocItem(
    docNumber: String,
    type: String,
    copyStatus: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = docNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text = "$type | $copyStatus", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Verifikasi Box") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}
