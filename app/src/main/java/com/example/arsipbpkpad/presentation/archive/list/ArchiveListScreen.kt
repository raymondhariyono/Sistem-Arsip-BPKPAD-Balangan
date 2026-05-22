package com.example.arsipbpkpad.presentation.archive.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import com.example.arsipbpkpad.presentation.components.BottomNavItem
import com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme
import com.example.arsipbpkpad.ui.theme.ChipBlue
import com.example.arsipbpkpad.ui.theme.ChipBlueBg
import com.example.arsipbpkpad.ui.theme.SuccessGreen
import com.example.arsipbpkpad.ui.theme.TextDark
import com.example.arsipbpkpad.ui.theme.TextPrimary
import com.example.arsipbpkpad.ui.theme.TextSecondary
import com.example.arsipbpkpad.ui.theme.TextTertiary

@Composable
fun ArchiveListScreen(
    viewModel: ArchiveListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArchiveListContent(
        uiState = uiState,
        onSearchQueryChange = { /* viewModel.onEvent(ArchiveListUiEvent.OnSearchQueryChange(it)) */ },
        onArchiveClick = onNavigateToDetail,
        onNavigateBack = onNavigateBack,
        onNavigateToBottomNav = onNavigateToBottomNav
    )
}

@Composable
fun ArchiveListContent(
    uiState: ArchiveListUiState,
    onSearchQueryChange: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit
) {
    Scaffold(
        topBar = {
            BpkpadTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                showProfile = true
            )
        },
        bottomBar = {
            BpkpadBottomNavigation(
                currentRoute = BottomNavItem.ARCHIVE.route,
                onNavigate = onNavigateToBottomNav
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_hint),
                                color = TextTertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextTertiary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Clear",
                                tint = TextTertiary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )
                }

                // Filter Row
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Filter",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        item { ActiveFilterChip(text = stringResource(R.string.filter_year)) }
                        item { InactiveFilterChip(text = stringResource(R.string.filter_type)) }
                        item { InactiveFilterChip(text = stringResource(R.string.filter_location)) }
                    }
                }

                // Results Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.search_results),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.files_found, uiState.archives.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Archive List
                items(uiState.archives) { archive ->
                    ArchiveCard(item = archive, onClick = { onArchiveClick(archive.id) })
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveFilterChip(text: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Default.Close,
            contentDescription = "Remove",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun InactiveFilterChip(text: String) {
    Row(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = TextDark,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = "Dropdown",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ArchiveCard(item: ArchiveDocument, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SP2D", // Placeholder for actual type if available in model
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ChipBlue,
                            modifier = Modifier
                                .background(ChipBlueBg, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    val isAvailable = true // Placeholder
                    val statusBgColor = if (isAvailable) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    val statusTextColor = if (isAvailable) MaterialTheme.colorScheme.primary else TextSecondary
                    val statusDotColor = if (isAvailable) SuccessGreen else TextTertiary
                    val statusText = if (isAvailable) stringResource(R.string.status_in_warehouse) else stringResource(R.string.status_borrowed)

                    Row(
                        modifier = Modifier
                            .background(statusBgColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LocationDetailItem(
                    label = stringResource(R.string.label_building),
                    value = "G1", // Placeholder
                    icon = Icons.Default.CheckCircle
                )
                LocationDetailItem(label = stringResource(R.string.label_rack), value = "C-42")
                LocationDetailItem(label = stringResource(R.string.label_box), value = "108")
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LocationDetailItem(label: String, value: String, icon: ImageVector? = null) {
    Column(modifier = Modifier.padding(end = 24.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun ArchiveListScreenPreview() {
    ArsipBPKPADTheme {
        ArchiveListContent(
            uiState = ArchiveListUiState(
                archives = listOf(
                    ArchiveDocument("1", "SP2D-1029", "Mock description", "2024-05-10", "Keuangan")
                )
            ),
            onSearchQueryChange = {},
            onArchiveClick = {},
            onNavigateBack = {},
            onNavigateToBottomNav = {}
        )
    }
}
