package com.example.arsipbpkpad.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToArchiveList: () -> Unit,
    onNavigateToScan: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Arsip BPKPAD",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNavigateToArchiveList,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(text = "Daftar Arsip")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToScan,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(text = "Scan Dokumen")
            }
        }
    }
}
