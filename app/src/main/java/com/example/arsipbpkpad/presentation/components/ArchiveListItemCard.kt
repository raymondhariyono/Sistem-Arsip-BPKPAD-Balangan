package com.example.arsipbpkpad.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.ui.theme.PrimaryGreen

@Composable
fun ArchiveTableHeader(
    showCondition: Boolean = true,
    showStatus: Boolean = true,
    showYear: Boolean = true
) {
    Surface(
        color = PrimaryGreen,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell(text = "No", width = 48.dp)
            VerticalDivider(color = Color.White.copy(alpha = 0.3f))
            HeaderCell(text = "Kode", width = 100.dp)
            VerticalDivider(color = Color.White.copy(alpha = 0.3f))
            HeaderCell(text = "No Dokumen", width = if (showYear) 200.dp else 264.dp)
            
            if (showYear) {
                VerticalDivider(color = Color.White.copy(alpha = 0.3f))
                HeaderCell(text = "Thn", width = 64.dp)
            }
            
            if (showCondition) {
                VerticalDivider(color = Color.White.copy(alpha = 0.3f))
                HeaderCell(text = "Kondisi", width = 100.dp)
            }
            
            if (showStatus) {
                VerticalDivider(color = Color.White.copy(alpha = 0.3f))
                HeaderCell(text = "Status", width = 120.dp)
            }
        }
    }
}

@Composable
fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ArchiveListItemCard(
    no: Int,
    archive: ArchiveDocument,
    onClick: () -> Unit,
    showCondition: Boolean = true,
    showStatus: Boolean = true,
    showYear: Boolean = true,
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
                .background(backgroundColor)
                .clickable { onClick() }
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. No
            TableCell(text = "$no.", width = 48.dp)
            VerticalDivider()

            // 2. Kode
            TableCell(text = archive.classificationCode, width = 100.dp, isBold = true, color = PrimaryGreen)
            VerticalDivider()

            // 3. No Dokumen
            TableCell(text = archive.documentNumber ?: "-", width = if (showYear) 200.dp else 264.dp, textAlign = TextAlign.Start)

            // 4. Tahun
            if (showYear) {
                VerticalDivider()
                TableCell(text = archive.year.toString(), width = 64.dp)
            }

            // 5. Kondisi
            if (showCondition) {
                VerticalDivider()
                Box(
                    modifier = Modifier.width(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ConditionBadge(condition = archive.condition.name)
                }
            }

            // 6. Status
            if (showStatus) {
                VerticalDivider()
                Box(
                    modifier = Modifier.width(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DocStatusBadge(status = archive.status.name)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
