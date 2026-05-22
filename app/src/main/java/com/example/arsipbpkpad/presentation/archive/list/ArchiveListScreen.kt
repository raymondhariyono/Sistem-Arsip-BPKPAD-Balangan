package com.example.arsipbpkpad.presentation.archive.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument

@Composable
fun ArchiveListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // For now, using dummy data to make it runnable and testable
    val dummyArchives = listOf(
        ArchiveDocument("1", "Surat Keputusan 01", "Deskripsi SK 01", "2023-01-01", "SK"),
        ArchiveDocument("2", "Laporan Keuangan Q1", "Deskripsi Laporan Q1", "2023-04-01", "Laporan"),
        ArchiveDocument("3", "Nota Dinas Pemberitahuan", "Deskripsi Nota Dinas", "2023-05-15", "Nota")
    )
    
    ArchiveListContent(
        uiState = ArchiveListUiState(archives = dummyArchives),
        onEvent = { event ->
            if (event is ArchiveListUiEvent.OnArchiveClick) {
                onNavigateToDetail(event.archiveId)
            }
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveListContent(
    uiState: ArchiveListUiState,
    onEvent: (ArchiveListUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.archive_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.archives) { archive ->
                        ArchiveItem(
                            archive = archive,
                            onClick = { onEvent(ArchiveListUiEvent.OnArchiveClick(archive.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveItem(
    archive: ArchiveDocument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = archive.title, style = MaterialTheme.typography.titleMedium)
            Text(text = archive.date, style = MaterialTheme.typography.bodySmall)
            Text(text = archive.category, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
