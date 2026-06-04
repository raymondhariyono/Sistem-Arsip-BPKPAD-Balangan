package com.example.arsipbpkpad.presentation.archive.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme

// --- STATE & EVENTS ---
data class ArchiveReviewUiState(
    val docNumber: String = "0982/SP2D/BPKPAD/2023",
    val subject: String = "Pembayaran Termin II Proyek Pembangunan Infrastruktur Jalan Raya Kecamatan ABC Tahun Anggaran 2023",
    val year: String = "2023",
    val warehouse: String = "Gudang A",
    val rack: String = "Rak-05-B",
    val isValidated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccessDialog: Boolean = false
)

sealed class ArchiveReviewUiEvent {
    data class OnDocNumberChange(val value: String) : ArchiveReviewUiEvent()
    data class OnSubjectChange(val value: String) : ArchiveReviewUiEvent()
    data class OnYearChange(val value: String) : ArchiveReviewUiEvent()
    data class OnWarehouseChange(val value: String) : ArchiveReviewUiEvent()
    data class OnRackChange(val value: String) : ArchiveReviewUiEvent()
    data class OnValidationToggle(val isValidated: Boolean) : ArchiveReviewUiEvent()
    data object OnSaveClick : ArchiveReviewUiEvent()
    data object DismissSuccessDialog : ArchiveReviewUiEvent()
}

// --- 1. STATEFUL COMPONENT ---
@Composable
fun ArchiveReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit, // Misal setelah simpan kembali ke Home
    viewModel: ArchiveReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArchiveReviewContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

// --- 2. STATELESS COMPONENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveReviewContent(
    uiState: ArchiveReviewUiState,
    onEvent: (ArchiveReviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    if (uiState.showSuccessDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { onEvent(ArchiveReviewUiEvent.DismissSuccessDialog) },
            confirmButton = {
                Button(onClick = { onEvent(ArchiveReviewUiEvent.DismissSuccessDialog) }) {
                    Text("OK")
                }
            },
            title = { Text("Berhasil") },
            text = { Text("Arsip berhasil disimpan ke sistem.") },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Halaman
            Text(
                text = stringResource(R.string.review_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.review_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- KARTU 1: Preview Dokumen ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.preview_doc_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.doc_type_detected), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Placeholder Gambar Dokumen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Gambar Hasil Scan", color = Color.Gray)

                        // Action Buttons Overlay (Zoom & Rotate)
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Zoom", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Icon(Icons.Default.Refresh, contentDescription = "Rotate", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- KARTU 2: Detail Ekstraksi ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.extraction_detail_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Form Fields
                    ExtractionTextField(
                        label = stringResource(R.string.label_no_doc),
                        value = uiState.docNumber,
                        onValueChange = { onEvent(ArchiveReviewUiEvent.OnDocNumberChange(it)) },
                        error = uiState.error?.takeIf { it.contains("Nomor", ignoreCase = true) }
                    )

                    ExtractionTextField(
                        label = stringResource(R.string.label_perihal),
                        value = uiState.subject,
                        onValueChange = { onEvent(ArchiveReviewUiEvent.OnSubjectChange(it)) },
                        singleLine = false,
                        minLines = 3
                    )

                    ExtractionDropdownField(
                        label = stringResource(R.string.label_tahun_anggaran),
                        value = uiState.year,
                        onValueChange = { onEvent(ArchiveReviewUiEvent.OnYearChange(it)) }
                    )

                    // Lokasi Fisik Row
                    Text(text = stringResource(R.string.label_lokasi_fisik), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExtractionTextField(
                            label = "", // No label, just field
                            value = uiState.warehouse,
                            onValueChange = { onEvent(ArchiveReviewUiEvent.OnWarehouseChange(it)) },
                            modifier = Modifier.weight(1f)
                        )
                        ExtractionTextField(
                            label = "", // No label, just field
                            value = uiState.rack,
                            onValueChange = { onEvent(ArchiveReviewUiEvent.OnRackChange(it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Validation Switch & Button
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Switch(
                            checked = uiState.isValidated,
                            onCheckedChange = { onEvent(ArchiveReviewUiEvent.OnValidationToggle(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.validation_switch_label), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { onEvent(ArchiveReviewUiEvent.OnSaveClick) },
                        enabled = uiState.isValidated && !uiState.isLoading, // Tombol aktif HANYA JIKA switch divalidasi
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.btn_simpan), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- MICRO-COMPONENTS ---
@Composable
fun ExtractionTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    error: String? = null
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        if (label.isNotEmpty()) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            isError = error != null,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun ExtractionDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("DD-MM-YYYY", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Logo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "BPKPAD Balangan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

// --- PREVIEW ---
@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun ArchiveReviewScreenPreview() {
    ArsipBPKPADTheme {
        ArchiveReviewContent(
            uiState = ArchiveReviewUiState(),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}