package com.example.arsipbpkpad.presentation.home.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.presentation.components.ArchiveListItemCard
import com.example.arsipbpkpad.presentation.components.ArchiveTableHeader

@Composable
fun HeaderSection() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val titleStyle = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall
    val subtitleStyle = if (isLandscape) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium
    val topPadding = if (isLandscape) 4.dp else 8.dp
    val bottomPadding = if (isLandscape) 2.dp else 4.dp

    Column(modifier = Modifier.padding(top = topPadding, bottom = bottomPadding)) {
        Text(
            text = stringResource(R.string.overview),
            style = titleStyle,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = subtitleStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PrimaryStatCard(
    title: String,
    count: String,
    containerColor: Color,
    contentColor: Color
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val titleStyle = if (isLandscape) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge
    val countStyle = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall
    val verticalPadding = if (isLandscape) 12.dp else 20.dp
    val spacerHeight = if (isLandscape) 4.dp else 12.dp

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = verticalPadding)) {
            Text(
                text = title,
                color = contentColor,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(spacerHeight))
            Text(
                text = count,
                color = contentColor,
                style = countStyle,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SecondaryStatCard(
    modifier: Modifier = Modifier,
    count: String,
    label: String
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val countStyle = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
    val labelStyle = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
    val padding = if (isLandscape) 8.dp else 16.dp
    val spacerHeight = if (isLandscape) 2.dp else 4.dp

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = countStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(spacerHeight))
            Text(
                text = label,
                style = labelStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onActionClick, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecentArchiveTable(items: List<ArchiveDocument>, onArchiveClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        ArchiveTableHeader(showCondition = false, showStatus = false)

        items.forEachIndexed { index, item ->
            ArchiveListItemCard(
                no = index + 1,
                archive = item,
                onClick = { onArchiveClick(item.id) },
                showCondition = false,
                showStatus = false
            )
        }
        
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_docs_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
