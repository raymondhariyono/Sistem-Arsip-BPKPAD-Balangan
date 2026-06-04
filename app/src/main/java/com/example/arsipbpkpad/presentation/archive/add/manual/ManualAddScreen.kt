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

    // Handle navigation after success
    androidx.compose.runtime.LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack() // Back to Archive List
            viewModel.onEvent(ManualAddUiEvent.ResetState)
        }
    }

    if (uiState.isSuccess) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Button(onClick = { /* Handled by LaunchedEffect */ }) {
                    Text("OK")
                }
            },
            title = { Text("Berhasil") },
            text = { Text("Arsip berhasil ditambahkan secara manual.") },
            icon = { Icon(Icons.Default.Done, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        )
    }

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
                    onOptionSelected = { onEvent(ManualAddUiEvent.OnDocTypeChange(it)) },
                    error = uiState.validationErrors["docType"]
                )
                FormTextField(
                    label = stringResource(R.string.label_doc_name),
                    placeholder = stringResource(R.string.hint_doc_name),
                    value = uiState.docName,
                    onValueChange = { onEvent(ManualAddUiEvent.OnDocNameChange(it)) },
                    error = uiState.validationErrors["docName"]
                )
                FormTextField(
                    label = stringResource(R.string.label_doc_number),
                    placeholder = stringResource(R.string.hint_doc_number),
                    value = uiState.docNumber,
                    onValueChange = { onEvent(ManualAddUiEvent.OnDocNumberChange(it)) },
                    error = uiState.validationErrors["docNumber"]
                )
                FormDropdownField(
                    label = stringResource(R.string.label_department),
                    placeholder = stringResource(R.string.hint_department),
                    value = uiState.department,
                    options = listOf("Dinas Pendidikan", "Dinas Kesehatan", "Dinas PU"),
                    onOptionSelected = { onEvent(ManualAddUiEvent.OnDepartmentChange(it)) },
                    error = uiState.validationErrors["department"]
                )
                FormTextField(
                    label = stringResource(R.string.label_year),
                    placeholder = "2024",
                    value = uiState.year,
                    onValueChange = { onEvent(ManualAddUiEvent.OnYearChange(it)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    error = uiState.validationErrors["year"]
                )
                FormTextField(
                    label = stringResource(R.string.label_validity),
                    placeholder = "DD-MM-YYYY",
                    value = uiState.validity,
                    onValueChange = { onEvent(ManualAddUiEvent.OnValidityChange(it)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    error = uiState.validationErrors["validity"]
                )
                FormTextField(
                    label = stringResource(R.string.label_subject),
                    placeholder = stringResource(R.string.hint_subject),
                    value = uiState.subject,
                    onValueChange = { onEvent(ManualAddUiEvent.OnSubjectChange(it)) },
                    singleLine = false,
                    minLines = 3,
                    error = uiState.validationErrors["subject"]
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormSectionCard(title = stringResource(R.string.section_location)) {
                FormDropdownField(
                    label = stringResource(R.string.label_warehouse),
                    placeholder = stringResource(R.string.hint_warehouse),
                    value = uiState.warehouse,
                    options = listOf("Gudang Utama", "Gudang Arsip 2", "Gudang Sementara"),
                    onOptionSelected = { onEvent(ManualAddUiEvent.OnWarehouseChange(it)) },
                    error = uiState.validationErrors["warehouse"]
                )
                FormTextField(
                    label = stringResource(R.string.label_rack),
                    placeholder = stringResource(R.string.hint_rack),
                    value = uiState.rackNo,
                    onValueChange = { onEvent(ManualAddUiEvent.OnRackNoChange(it)) },
                    error = uiState.validationErrors["rackNo"]
                )
                FormTextField(
                    label = stringResource(R.string.label_box),
                    placeholder = stringResource(R.string.hint_box),
                    value = uiState.boxNo,
                    onValueChange = { onEvent(ManualAddUiEvent.OnBoxNoChange(it)) },
                    error = uiState.validationErrors["boxNo"]
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onEvent(ManualAddUiEvent.OnSaveClick) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.btn_save_archive),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    minLines: Int = 1,
    error: String? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
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
            isError = error != null,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdownField(
    label: String,
    placeholder: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    error: String? = null
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
                isError = error != null,
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
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
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
