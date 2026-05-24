package com.example.arsipbpkpad.presentation.archive.export

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme
import java.text.NumberFormat
import java.util.Locale

// --- STATE & EVENTS ---
data class ExportUiState(
    val availableYears: List<String> = listOf("2024", "2023", "2022", "2021", "2020", "2019", "2018", "Lainnya"),
    val selectedYears: Set<String> = setOf("2024", "2023"), // Default selection based on mockup
    val estimatedRowsPerYear: Int = 620, // Dummy data for calculation
    val isExporting: Boolean = false
) {
    val totalEstimatedRows: Int
        get() = selectedYears.size * estimatedRowsPerYear

    val selectedYearsText: String
        get() = if (selectedYears.isEmpty()) "-" else selectedYears.sortedDescending().joinSequence()

    private fun Iterable<String>.joinSequence() = joinToString(", ")
}

sealed class ExportUiEvent {
    data class OnToggleYear(val year: String) : ExportUiEvent()
    data object OnSelectAllClick : ExportUiEvent()
    data object OnExportClick : ExportUiEvent()
}

// --- 1. STATEFUL COMPONENT ---
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit
) {
    var uiState by remember { mutableStateOf(ExportUiState()) }

    ExportContent(
        uiState = uiState,
        onEvent = { event ->
            uiState = when (event) {
                is ExportUiEvent.OnToggleYear -> {
                    val newSelection = if (uiState.selectedYears.contains(event.year)) {
                        uiState.selectedYears - event.year
                    } else {
                        uiState.selectedYears + event.year
                    }
                    uiState.copy(selectedYears = newSelection)
                }
                is ExportUiEvent.OnSelectAllClick -> {
                    if (uiState.selectedYears.size == uiState.availableYears.size) {
                        uiState.copy(selectedYears = emptySet()) // Deselect all if already all selected
                    } else {
                        uiState.copy(selectedYears = uiState.availableYears.toSet())
                    }
                }
                is ExportUiEvent.OnExportClick -> {
                    // TODO: Trigger actual export process
                    uiState
                }
            }
        },
        onNavigateBack = onNavigateBack
    )
}

// --- 2. STATELESS COMPONENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportContent(
    uiState: ExportUiState,
    onEvent: (ExportUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = { ExportTopAppBar(onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Main Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Subtitle
            Text(
                text = stringResource(R.string.export_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.export_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card: Year Selection
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f), // Takes remaining space gracefully
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Header Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.select_year_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )

                        TextButton(onClick = { onEvent(ExportUiEvent.OnSelectAllClick) }) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.select_all), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Grid of Years
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f) // Push summary to bottom
                    ) {
                        items(uiState.availableYears) { year ->
                            YearSelectionChip(
                                year = year,
                                isSelected = uiState.selectedYears.contains(year),
                                onClick = { onEvent(ExportUiEvent.OnToggleYear(year)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary Box
                    ExportSummaryBox(
                        selectedCount = uiState.selectedYears.size,
                        selectedYearsText = uiState.selectedYearsText,
                        totalRows = uiState.totalEstimatedRows
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Export Button
            Button(
                onClick = { onEvent(ExportUiEvent.OnExportClick) },
                enabled = uiState.selectedYears.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B5E20),
                    disabledContainerColor = Color(0xFFBDBDBD)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_export_excel),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- MICRO-COMPONENTS ---

@Composable
fun YearSelectionChip(
    year: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFE0E0E0) else Color(0xFFE0E0E0)
    val textColor = Color(0xFF424242)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = year, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)

        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
        } else {
            Icon(Icons.Outlined.Info, contentDescription = "Unselected", tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ExportSummaryBox(
    selectedCount: Int,
    selectedYearsText: String,
    totalRows: Int
) {
    val formattedRows = NumberFormat.getNumberInstance(Locale("id", "ID")).format(totalRows)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFF2E7D32), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = stringResource(R.string.export_summary_title), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.export_summary_desc, selectedCount, selectedYearsText, formattedRows),
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.export_summary_info), fontSize = 10.sp, color = Color(0xFF757575))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Logo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
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
fun ExportScreenPreview() {
    ArsipBPKPADTheme {
        ExportContent(
            uiState = ExportUiState(),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}