package com.example.arsipbpkpad.presentation.archive.detail

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.UserRole
import com.example.arsipbpkpad.domain.model.canMutateArchive
import com.example.arsipbpkpad.presentation.components.BpkpadConfirmDialog
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.presentation.components.DialogType
import com.example.arsipbpkpad.presentation.components.DocStatusBadge
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ArchiveDetailScreen(
    archiveId: String,
    userRole: UserRole = UserRole.UNKNOWN,
    onNavigateBack: () -> Unit,
    onNavigateToArchive: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: ArchiveDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button specifically
    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    ArchiveDetailContent(
        state = state,
        userRole = userRole,
        onNavigateBack = onNavigateBack,
        onEditClick = { onNavigateToEdit(archiveId) },
        onDeleteClick = {
            viewModel.deleteArchive { onNavigateBack() }
        },
        onNavigateToArchive = onNavigateToArchive
    )
}

@Composable
fun ArchiveDetailContent(
    state: ArchiveDetailState,
    userRole: UserRole,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNavigateToArchive: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        BpkpadConfirmDialog(
            title = stringResource(R.string.title_delete_archive),
            message = stringResource(R.string.msg_delete_archive_confirm),
            confirmText = stringResource(R.string.btn_delete),
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = {
                onDeleteClick()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            type = DialogType.DESTRUCTIVE
        )
    }

    Scaffold(
        topBar = {
            ArchiveDetailTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                state.isLoading -> LoadingState(Modifier.align(Alignment.Center))
                state.archive != null -> {
                    ArchiveDetailMainList(
                        archive = state.archive,
                        relatedDocs = state.relatedBundleDocuments,
                        activityLogs = state.activityLogs,
                        activityLogsErrorMessage = state.activityLogsErrorMessage,
                        userRole = userRole,
                        onEditClick = onEditClick,
                        onDeleteClick = { showDeleteDialog = true },
                        onNavigateToArchive = onNavigateToArchive
                    )
                }
                state.errorMessage != null -> ErrorState(state.errorMessage, Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun ArchiveDetailTopBar(onNavigateBack: () -> Unit) {
    BpkpadTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.label_financial_dashboard),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
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
fun LoadingState(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable
fun ArchiveDetailMainList(
    archive: ArchiveDocument,
    relatedDocs: List<ArchiveDocument>,
    activityLogs: List<com.example.arsipbpkpad.domain.model.ActivityLog>,
    activityLogsErrorMessage: String?,
    userRole: com.example.arsipbpkpad.domain.model.UserRole,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNavigateToArchive: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ArchiveDetailHeader(archive) }
        item { ArchiveFinancialCard(archive) }
        item { ArchivePhysicalInfoCard(archive) }

        if (relatedDocs.isNotEmpty()) {
            item { RelatedBundleHeader() }
            items(relatedDocs) { related ->
                RelatedArchiveItem(related = related, onClick = { onNavigateToArchive(related.id) })
            }
        }

        if (userRole.canMutateArchive()) {
            item {
                DetailActionButtons(
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    userRole = userRole
                )
            }
        }

        item { ActivityHistoryHeader() }
        
        when {
            activityLogsErrorMessage != null -> {
                item {
                    Text(
                        text = "Gagal memuat riwayat: $activityLogsErrorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            activityLogs.isEmpty() -> {
                item {
                    Text(
                        text = "Belum ada riwayat aktivitas yang tercatat untuk dokumen ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            else -> {
                items(activityLogs) { log ->
                    ActivityLogTimelineItem(log)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun ArchiveDetailHeader(archive: ArchiveDocument) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = archive.type.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = archive.documentNumber ?: "-",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            DocStatusBadge(status = archive.status.name)
        }
    }
}

@Composable
fun ArchiveFinancialCard(archive: ArchiveDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.label_nominal_transaction),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            Text(
                text = formatter.format(archive.nominal ?: 0.0),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.label_description),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = archive.description ?: stringResource(R.string.label_no_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun ArchivePhysicalInfoCard(archive: ArchiveDocument) {
    DetailCardContainer(
        title = stringResource(R.string.label_physical_info),
        icon = Icons.Default.LocationOn
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LocationBlock(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_warehouse_caps),
                value = archive.metadata?.warehouse ?: "-"
            )
            LocationBlock(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_rak_caps),
                value = archive.metadata?.rack ?: "-"
            )
            LocationBlock(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_box_caps),
                value = archive.metadata?.boxNumber ?: "-",
                isHighlighted = true
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_copy_type),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = archive.copyType.toDisplayString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.label_kondisi),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = archive.condition.toDisplayString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.label_amount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.label_sheets, archive.copyCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RelatedBundleHeader() {
    Text(
        text = stringResource(R.string.label_bundle_transaction),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun RelatedArchiveItem(related: ArchiveDocument, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = related.documentNumber ?: "-",
                    fontWeight = FontWeight.Bold
                )
            },
            supportingContent = {
                Text(text = related.type.name)
            },
            leadingContent = {
                Box(
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun DetailActionButtons(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    userRole: UserRole = UserRole.UNKNOWN
) {
    if (!userRole.canMutateArchive()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Edit Arsip",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        androidx.compose.material3.OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Hapus Arsip",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun DetailCardContainer(
    title: String,
    icon: ImageVector,
    actionIcon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (actionIcon != null) {
                    Icon(imageVector = actionIcon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun LocationBlock(modifier: Modifier = Modifier, label: String, value: String, isHighlighted: Boolean = false) {
    val bgColor = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val labelColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = labelColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = textColor)
    }
}

@Composable
fun ActivityHistoryHeader() {
    Text(
        text = "Riwayat Aktivitas",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
fun ActivityLogTimelineItem(log: com.example.arsipbpkpad.domain.model.ActivityLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (log.action) {
                    "CREATE" -> Icons.Default.Add
                    "UPDATE" -> Icons.Default.Edit
                    "DELETE" -> Icons.Default.Delete
                    else -> Icons.Default.Info
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.action,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = log.details ?: "No details provided",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                log.timestamp?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.arsipbpkpad.utils.DateUtils.formatDateTime(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ArchiveDetailPreview() {
    val mockArchive = com.example.arsipbpkpad.domain.model.ArchiveDocument(
        id = "1",
        type = com.example.arsipbpkpad.domain.model.DocType.SP2D,
        documentNumber = "001/SP2D/2026",
        copyType = com.example.arsipbpkpad.domain.model.DocCopyType.ORIGINAL,
        copyCount = 1,
        description = "Belanja ATK Kantor",
        nominal = 1500000.0,
        condition = com.example.arsipbpkpad.domain.model.DocCondition.GOOD,
        status = com.example.arsipbpkpad.domain.model.DocStatus.AVAILABLE,
        metadata = com.example.arsipbpkpad.domain.model.ArchiveMetadata(
            warehouse = "Gedung A",
            rack = "R-01",
            boxNumber = "B-101"
        )
    )
    ArsipBPKPADTheme {
        ArchiveDetailContent(
            state = ArchiveDetailState(
                archive = mockArchive
            ),
            userRole = com.example.arsipbpkpad.domain.model.UserRole.ARSIPARIS,
            onNavigateBack = {},
            onEditClick = {},
            onDeleteClick = {},
            onNavigateToArchive = {}
        )
    }
}
