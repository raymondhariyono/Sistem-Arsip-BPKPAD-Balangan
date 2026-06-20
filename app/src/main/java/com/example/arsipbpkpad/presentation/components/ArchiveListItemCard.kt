package com.example.arsipbpkpad.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.arsipbpkpad.domain.model.ArchiveDocument

@Composable
fun ArchiveTableHeader() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell(text = "No", weight = 0.08f)
            VerticalDivider()
            HeaderCell(text = "Kode", weight = 0.20f)
            VerticalDivider()
            HeaderCell(text = "No Dokumen", weight = 0.30f)
            VerticalDivider()
            HeaderCell(text = "Thn", weight = 0.12f)
            VerticalDivider()
            HeaderCell(text = "Kondisi", weight = 0.15f)
            VerticalDivider()
            HeaderCell(text = "Status", weight = 0.15f)
        }
    }
}

@Composable
fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ArchiveListItemCard(
    no: Int,
    archive: ArchiveDocument,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (no % 2 == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onClick() }
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. No
            TableCell(text = "$no.", weight = 0.08f)
            VerticalDivider()

            // 2. Kode
            TableCell(text = archive.classificationCode, weight = 0.20f, isBold = true, color = MaterialTheme.colorScheme.primary)
            VerticalDivider()

            // 3. No Dokumen
            TableCell(text = archive.documentNumber ?: "-", weight = 0.30f, textAlign = TextAlign.Start)
            VerticalDivider()

            // 4. Tahun
            TableCell(text = archive.year.toString(), weight = 0.12f)
            VerticalDivider()

            // 5. Kondisi
            Box(
                modifier = Modifier.weight(0.15f),
                contentAlignment = Alignment.Center
            ) {
                ConditionBadge(condition = archive.condition.name)
            }
            VerticalDivider()

            // 6. Status
            Box(
                modifier = Modifier.weight(0.15f),
                contentAlignment = Alignment.Center
            ) {
                DocStatusBadge(status = archive.status.name)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isBold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
