package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme

@Composable
fun ManualAddScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManualAddViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ManualAddContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddContent(
    uiState: ManualAddUiState,
    onEvent: (ManualAddUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { ManualAddTopAppBar(onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.manual_add_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.manual_add_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            FormSectionCard(title = stringResource(R.string.section_doc_detail)) {
                FormDropdownField(
                    label = stringResource(R.string.label_doc_type),
                    placeholder = stringResource(R.string.hint_doc_type),
                    value = uiState.docType,
                    options = listOf("SP2D", "SPM", "SP3B", "DSB"),
                    onOptionSelected = { onEvent(ManualAddUiEvent.OnDocTypeChange(it)) }
                )
                FormTextField(
                    label = stringResource(R.string.label_doc_name),
                    placeholder = stringResource(R.string.hint_doc_name),
                    value = uiState.docName,
                    onValueChange = { onEvent(ManualAddUiEvent.OnDocNameChange(it)) }
                )
                FormTextField(
                    label = stringResource(R.string.label_doc_number),
                    placeholder = stringResource(R.string.hint_doc_number),
                    value = uiState.docNumber,
                    onValueChange = { onEvent(ManualAddUiEvent.OnDocNumberChange(it)) }
                )
                FormDropdownField(
                    label = stringResource(R.string.label_department),
                    placeholder = stringResource(R.string.hint_department),
                    value = uiState.department,
                    options = listOf("Dinas Pendidikan", "Dinas Kesehatan", "Dinas PU"),
                    onOptionSelected = { onEvent(ManualAddUiEvent.OnDepartmentChange(it)) }
                )
                FormTextField(
                    label = stringResource(R.string.label_year),
                    placeholder = stringResource(R.string.hint_year_format),
                    value = uiState.year,
                    onValueChange = { onEvent(ManualAddUiEvent.OnYearChange(it)) }
                )
                FormTextField(
                    label = stringResource(R.string.label_validity),
                    placeholder = stringResource(R.string.hint_validity),
                    value = uiState.validity,
                    onValueChange = { onEvent(ManualAddUiEvent.OnValidityChange(it)) }
                )
                FormTextField(
                    label = stringResource(R.string.label_subject),
                    placeholder = stringResource(R.string.hint_subject),
                    value = uiState.subject,
                    onValueChange = { onEvent(ManualAddUiEvent.OnSubjectChange(it)) },
                    singleLine = false,
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormSectionCard(title = stringResource(R.string.section_location)) {
                FormDropdownField(
                    label = stringResource(R.string.label_warehouse),
                    placeholder = stringResource(R.string.hint_warehouse),
                    value = uiState.warehouse,
                    options = listOf("Gudang Utama", "Gudang Arsip 2", "Gudang Sementara"),
                    onOptionSelected = { onEvent(ManualAddUiEvent.OnWarehouseChange(it)) }
                )
                FormTextField(
                    label = stringResource(R.string.label_rack),
                    placeholder = stringResource(R.string.hint_rack),
                    value = uiState.rackNo,
                    onValueChange = { onEvent(ManualAddUiEvent.OnRackNoChange(it)) }
                )
                FormTextField(
                    label = stringResource(R.string.label_box),
                    placeholder = stringResource(R.string.hint_box),
                    value = uiState.boxNo,
                    onValueChange = { onEvent(ManualAddUiEvent.OnBoxNoChange(it)) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onEvent(ManualAddUiEvent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_save_archive),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Done, 
                    contentDescription = stringResource(R.string.logo_desc), 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name_full), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = FontWeight.SemiBold, 
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    text = placeholder, 
                    color = MaterialTheme.colorScheme.outline, 
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdownField(
    label: String,
    placeholder: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = FontWeight.SemiBold, 
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = { 
                    Text(
                        text = placeholder, 
                        color = MaterialTheme.colorScheme.outline, 
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option, 
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun ManualAddScreenPreview() {
    ArsipBPKPADTheme {
        ManualAddContent(
            uiState = ManualAddUiState(),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}
