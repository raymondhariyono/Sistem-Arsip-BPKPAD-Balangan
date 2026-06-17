package com.example.arsipbpkpad.presentation.archive.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.presentation.components.BottomNavItem
import com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation
import com.example.arsipbpkpad.presentation.components.BpkpadExpandableFAB
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme
import kotlinx.coroutines.flow.flowOf

@Composable
fun ArchiveListScreen(
    year: Int? = null,
    viewModel: ArchiveListViewModel = hiltViewModel(),
    stagingViewModel: com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRapidInput: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stagingState by stagingViewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.archivesPagingData.collectAsLazyPagingItems()
    
    LaunchedEffect(year) {
        if (year != null) {
            viewModel.updateInitialYear(year)
        }
    }

    LaunchedEffect(stagingState.isBoxContextSet) {
        if (stagingState.isBoxContextSet) {
            onNavigateToRapidInput()
        }
    }

    Scaffold(
        topBar = {
            com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar(
                title = {
                    Text(
                        text = if (uiState.isFilterConfirmed) "Daftar Arsip" else "Archival Repository",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isFilterConfirmed) {
                            viewModel.onEvent(ArchiveListUiEvent.OnResetFilter)
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            BpkpadBottomNavigation(
                currentRoute = BottomNavItem.ARCHIVE.route,
                onNavigate = onNavigateToBottomNav
            )
        },
        floatingActionButton = {
            if (uiState.isFilterConfirmed) {
                BpkpadExpandableFAB(
                    onManualInputClick = { 
                        onNavigateToBottomNav(BottomNavItem.ADD)
                    },
                    onOcrScanClick = onNavigateToScan
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.selectedYears.isEmpty() && !uiState.isFilterConfirmed) {
                // Year Grid Selection (Entry Point)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ArchivalRepositoryHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val yearsRange = (2016..2026).reversed().toList()
                        yearsRange.chunked(2).forEach { rowYears ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowYears.forEach { y ->
                                        val stats = uiState.yearStats.find { it.year == y }
                                        YearGridCard(
                                            year = y,
                                            recordsCount = stats?.count ?: 0,
                                            lastUpdated = stats?.lastUpdated ?: "No data",
                                            onClick = { viewModel.updateInitialYear(y) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowYears.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            } else {
                ArchiveListContentOnly(
                    uiState = uiState,
                    pagingItems = pagingItems,
                    onSearchQueryChange = { query -> 
                        viewModel.onEvent(ArchiveListUiEvent.OnSearchQueryChange(query)) 
                    },
                    onFilterChange = { type ->
                        viewModel.onEvent(ArchiveListUiEvent.OnFilterChange(type))
                    },
                    onArchiveClick = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
fun ArchivalRepositoryHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Archival\nRepository",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pilih tahun arsip untuk melihat daftar dokumen",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun YearGridCard(
    year: Int,
    recordsCount: Int,
    lastUpdated: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$recordsCount Records",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "Updated $lastUpdated",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.LightGray
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF1B5E20),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyArchiveState(onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum ada tahun yang dipilih", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onAction) { Text("Pilih Tahun di Dashboard") }
    }
}

@Composable
fun ArchiveListContentOnly(
    uiState: ArchiveListUiState,
    pagingItems: LazyPagingItems<ArchiveDocument>,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        // Header section with search and filter
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // Active Filters Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Tahun ${uiState.selectedYears.sortedDescending().joinToString(", ")}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_hint),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Doc Type Filter
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf("Semua", "SP2D", "SPM", "SPP", "SPJ")
                items(filters) { filter ->
                    FilterChip(
                        text = filter,
                        isSelected = uiState.selectedFilter == filter,
                        onClick = { onFilterChange(filter) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dynamic Table Layout
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item { TableHeader() }

            items(pagingItems.itemCount) { index ->
                val archive = pagingItems[index]
                if (archive != null) {
                    ArchiveTableRow(
                        no = (index + 1).toString(),
                        archive = archive,
                        onClick = { onArchiveClick(archive.id) }
                    )
                }
            }

            // Load States and Empty state
            if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tidak ada dokumen ditemukan", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            pagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = "No", weight = 0.1f, isHeader = true)
        TableCell(text = "No. Dokumen", weight = 0.35f, isHeader = true)
        TableCell(text = "Tahun", weight = 0.15f, isHeader = true)
        TableCell(text = "Gudang-Rak-Box", weight = 0.25f, isHeader = true)
        TableCell(text = "Status", weight = 0.15f, isHeader = true)
    }
}

@Composable
fun ArchiveTableRow(
    no: String,
    archive: ArchiveDocument,
    onClick: () -> Unit
) {
    // Logic for 10-year retention cue
    val currentYear = 2026
    val mustBeDestroyed = currentYear > (archive.year + 10)
    
    val backgroundColor = if (mustBeDestroyed) Color(0xFFFFEBEE) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = no, weight = 0.1f)
        
        Row(modifier = Modifier.weight(0.35f), verticalAlignment = Alignment.CenterVertically) {
            if (mustBeDestroyed) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(14.dp).padding(end = 4.dp)
                )
            }
            Text(
                text = archive.documentNumber ?: "-",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        TableCell(text = archive.year.toString(), weight = 0.15f)
        
        val locationText = if (archive.idStorageLocation != null) {
            "G1-C42-108" 
        } else {
            "-"
        }
        TableCell(text = locationText, weight = 0.25f)

        val statusText = when (archive.status) {
            DocStatus.AVAILABLE -> "Tersedia"
            DocStatus.BORROWED -> "Dipinjam"
            DocStatus.DISPOSED -> "Dimusnahkan"
            DocStatus.UNVERIFIED -> "Belum Verifikasi"
        }
        TableCell(text = statusText, weight = 0.15f)
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        style = if (isHeader) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) 
                else MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(containerColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun ArchiveListScreenPreview() {
    ArsipBPKPADTheme {
        val mockPagingData = PagingData.from(listOf(
            ArchiveDocument(
                id = "1",
                type = com.example.arsipbpkpad.domain.model.DocType.SP2D,
                documentNumber = "SP2D-1029",
                copyType = com.example.arsipbpkpad.domain.model.DocCopyType.ORIGINAL,
                copyCount = 1,
                nominal = 1000000.0,
                description = "Pembayaran ATK",
                year = 2014,
                status = DocStatus.AVAILABLE,
                idStorageLocation = "LOC1",
                metadata = null,
                createdBy = null,
                verifiedBy = null,
                createdAt = "2014-05-10",
                updatedAt = null
            )
        ))
        val pagingItems = flowOf(mockPagingData).collectAsLazyPagingItems()
        
        ArchiveListContentOnly(
            uiState = ArchiveListUiState(
                isFilterConfirmed = true,
                selectedYears = setOf(2014),
                selectedFilter = "SP2D"
            ),
            pagingItems = pagingItems,
            onSearchQueryChange = {},
            onFilterChange = {},
            onArchiveClick = {}
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
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
