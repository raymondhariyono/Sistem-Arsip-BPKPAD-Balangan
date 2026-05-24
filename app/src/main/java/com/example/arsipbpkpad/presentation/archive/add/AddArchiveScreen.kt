package com.example.arsipbpkpad.presentation.archive.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.presentation.components.BottomNavItem
import com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme
import com.example.arsipbpkpad.ui.theme.LightGreen

@Composable
fun AddArchiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScanOcr: () -> Unit,
    onNavigateToManualAdd: () -> Unit,
    onNavigateToImportSpreadsheet: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit
) {
    AddArchiveContent(
        onNavigateBack = onNavigateBack,
        onNavigateToScanOcr = onNavigateToScanOcr,
        onNavigateToManualAdd = onNavigateToManualAdd,
        onNavigateToImportSpreadsheet = onNavigateToImportSpreadsheet,
        onNavigateToBottomNav = onNavigateToBottomNav
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArchiveContent(
    onNavigateBack: () -> Unit,
    onNavigateToScanOcr: () -> Unit,
    onNavigateToManualAdd: () -> Unit,
    onNavigateToImportSpreadsheet: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
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
                }
            )
        },
        bottomBar = {
            BpkpadBottomNavigation(
                currentRoute = BottomNavItem.ADD.route,
                onNavigate = onNavigateToBottomNav
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.add_archive_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.add_archive_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            MethodSelectionCard(
                icon = Icons.Default.Add,
                iconBgColor = LightGreen,
                iconTintColor = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.method_scan_title),
                description = stringResource(R.string.method_scan_desc),
                actionText = stringResource(R.string.method_scan_action),
                onClick = onNavigateToScanOcr
            )

            Spacer(modifier = Modifier.height(16.dp))

            MethodSelectionCard(
                icon = Icons.Default.Menu,
                iconBgColor = LightGreen,
                iconTintColor = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.method_manual_title),
                description = stringResource(R.string.method_manual_desc),
                onClick = onNavigateToManualAdd
            )

            Spacer(modifier = Modifier.height(16.dp))

            MethodSelectionCard(
                icon = Icons.Default.Create,
                iconBgColor = LightGreen,
                iconTintColor = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.method_import_title),
                description = stringResource(R.string.method_import_desc),
                onClick = onNavigateToImportSpreadsheet
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MethodSelectionCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (actionText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun AddArchiveScreenPreview() {
    ArsipBPKPADTheme {
        AddArchiveContent(
            onNavigateBack = {},
            onNavigateToScanOcr = {},
            onNavigateToManualAdd = {},
            onNavigateToImportSpreadsheet = {},
            onNavigateToBottomNav = {}
        )
    }
}
