package com.example.arsipbpkpad.presentation.archive.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme

// --- STATE MODEL ---
data class ArchiveDetailUiModel(
    val title: String = "SP2D-2023-11-0045",
    val type: String = "SP2D",
    val isVerified: Boolean = true,
    val uploadDate: String = "Nov 12, 2023",
    val uploaderName: String = "Admin",
    val docName: String = "Surat Perintah Pencairan Dana (Dinas Pendidikan)",
    val docNumber: String = "REF/PEND/2023/11/45",
    val department: String = "Dinas Pendidikan dan Kebudayaan",
    val docDate: String = "10 November 2023",
    val validity: String = "10 Tahun (Expired 2033)",
    val warehouse: String = "G1",
    val rack: String = "12",
    val box: String = "5"
)

// --- 1. STATEFUL COMPONENT ---
@Composable
fun ArchiveDetailScreen(
    archiveId: String,
    onNavigateBack: () -> Unit
) {
    // Mock State (Nanti diganti dengan ViewModel)
    val uiState = ArchiveDetailUiModel()

    ArchiveDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEditClick = { /* Handle Edit */ },
        onExportClick = { /* Handle Export */ }
    )
}

// --- 2. STATELESS COMPONENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveDetailContent(
    uiState: ArchiveDetailUiModel,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.archive_detail_title), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu Option */ }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- HEADER: Badge, Title & Actions ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tipe Badge
                Text(
                    text = uiState.type,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424242),
                    modifier = Modifier.background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Verified Badge
                if (uiState.isVerified) {
                    Row(
                        modifier = Modifier.background(Color(0xFFCBFFC2), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.status_verified), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = uiState.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(R.string.uploaded_info, uiState.uploadDate, uiState.uploaderName), fontSize = 12.sp, color = Color(0xFF757575))

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onEditClick,
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_edit_detail), color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onExportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- KARTU 1: Digital Scan ---
            DetailCardContainer(
                title = stringResource(R.string.section_digital_scan),
                icon = Icons.Default.AddCircle,
                actionIcon = Icons.Default.Menu
            ) {
                // Placeholder Image Scan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("(TABLE DATA)", color = Color.LightGray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- KARTU 2: Metadata ---
            DetailCardContainer(
                title = stringResource(R.string.section_metadata),
                icon = Icons.Default.Info
            ) {
                MetadataRow(label = stringResource(R.string.label_nama_dokumen), value = uiState.docName)
                MetadataRow(label = stringResource(R.string.label_nomor_dokumen), value = uiState.docNumber)
                MetadataRow(label = stringResource(R.string.label_dinas), value = uiState.department)
                MetadataRow(label = stringResource(R.string.label_tanggal_dokumen), value = uiState.docDate)
                MetadataRow(label = stringResource(R.string.label_masa_berlaku), value = uiState.validity, showDivider = false)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- KARTU 3: Lokasi Fisik ---
            DetailCardContainer(
                title = stringResource(R.string.section_physical_location),
                icon = Icons.Default.LocationOn
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LocationBlock(modifier = Modifier.weight(1f), label = "GUDANG", value = uiState.warehouse)
                    LocationBlock(modifier = Modifier.weight(1f), label = "RAK", value = uiState.rack)
                    LocationBlock(modifier = Modifier.weight(1f), label = "BOX", value = uiState.box, isHighlighted = true)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- MICRO-COMPONENTS ---

@Composable
fun DetailCardContainer(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                }
                if (actionIcon != null) {
                    Icon(imageVector = actionIcon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String, showDivider: Boolean = true) {
    Column {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9E9E9E))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 13.sp, color = Color(0xFF212121), lineHeight = 18.sp)
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE), thickness = 1.dp)
        }
    }
}

@Composable
fun LocationBlock(modifier: Modifier = Modifier, label: String, value: String, isHighlighted: Boolean = false) {
    val bgColor = if (isHighlighted) Color(0xFF1B5E20) else Color(0xFFE8F5E9)
    val textColor = if (isHighlighted) Color.White else Color(0xFF2E7D32)
    val labelColor = if (isHighlighted) Color(0xFFA5D6A7) else Color(0xFF757575)

    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = labelColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
    }
}

// --- PREVIEW ---
@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun ArchiveDetailPreview() {
    ArsipBPKPADTheme {
        ArchiveDetailContent(
            uiState = ArchiveDetailUiModel(),
            onNavigateBack = {},
            onEditClick = {},
            onExportClick = {}
        )
    }
}