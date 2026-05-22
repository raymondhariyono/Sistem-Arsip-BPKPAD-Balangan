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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.ui.theme.ArsipBPKPADTheme

// --- STATE & EVENTS ---
data class ManualAddUiState(
    val docType: String = "",
    val docName: String = "",
    val docNumber: String = "",
    val department: String = "",
    val year: String = "",
    val validity: String = "",
    val subject: String = "",
    val warehouse: String = "",
    val rackNo: String = "",
    val boxNo: String = ""
)

sealed class ManualAddUiEvent {
    data class OnDocTypeChange(val value: String) : ManualAddUiEvent()
    data class OnDocNameChange(val value: String) : ManualAddUiEvent()
    data class OnDocNumberChange(val value: String) : ManualAddUiEvent()
    data class OnDepartmentChange(val value: String) : ManualAddUiEvent()
    data class OnYearChange(val value: String) : ManualAddUiEvent()
    data class OnValidityChange(val value: String) : ManualAddUiEvent()
    data class OnSubjectChange(val value: String) : ManualAddUiEvent()
    data class OnWarehouseChange(val value: String) : ManualAddUiEvent()
    data class OnRackNoChange(val value: String) : ManualAddUiEvent()
    data class OnBoxNoChange(val value: String) : ManualAddUiEvent()
    data object OnSaveClick : ManualAddUiEvent()
}

// --- 1. STATEFUL COMPONENT ---
@Composable
fun ManualAddScreen(
    onNavigateBack: () -> Unit,
    // viewModel: ManualAddViewModel = hiltViewModel() // Nanti kita pasang ViewModel
) {
    // Mock State (Ganti dengan viewModel.uiState.collectAsState() nanti)
    var uiState by remember { mutableStateOf(ManualAddUiState()) }

    ManualAddContent(
        uiState = uiState,
        onEvent = { event ->
            // Mock Event Handler
            uiState = when (event) {
                is ManualAddUiEvent.OnDocTypeChange -> uiState.copy(docType = event.value)
                is ManualAddUiEvent.OnDocNameChange -> uiState.copy(docName = event.value)
                is ManualAddUiEvent.OnDocNumberChange -> uiState.copy(docNumber = event.value)
                is ManualAddUiEvent.OnDepartmentChange -> uiState.copy(department = event.value)
                is ManualAddUiEvent.OnYearChange -> uiState.copy(year = event.value)
                is ManualAddUiEvent.OnValidityChange -> uiState.copy(validity = event.value)
                is ManualAddUiEvent.OnSubjectChange -> uiState.copy(subject = event.value)
                is ManualAddUiEvent.OnWarehouseChange -> uiState.copy(warehouse = event.value)
                is ManualAddUiEvent.OnRackNoChange -> uiState.copy(rackNo = event.value)
                is ManualAddUiEvent.OnBoxNoChange -> uiState.copy(boxNo = event.value)
                is ManualAddUiEvent.OnSaveClick -> {
                    // Handle simpan
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
fun ManualAddContent(
    uiState: ManualAddUiState,
    onEvent: (ManualAddUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { ManualAddTopAppBar() },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Halaman
            Text(
                text = stringResource(R.string.manual_add_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.manual_add_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1: Detail Dokumen
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
                    placeholder = stringResource(R.string.hint_year),
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

            // Card 2: Lokasi Gudang
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

            // Tombol Simpan
            Button(
                onClick = { onEvent(ManualAddUiEvent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_save_archive),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(88.dp)) // Jarak untuk Bottom Nav
        }
    }
}

// --- 3. MICRO-COMPONENTS (REUSABLE FORM) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddTopAppBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Done, contentDescription = "Logo", tint = Color(0xFF2E7D32), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "BPKPAD Balangan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
            }
        },
        actions = {
            IconButton(
                onClick = { },
                modifier = Modifier.padding(end = 16.dp).clip(CircleShape).background(Color(0xFFE8F5E9)).size(36.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
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
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = Color.LightGray, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2E7D32),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
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
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true, // Supaya tidak bisa diketik manual
                placeholder = { Text(text = placeholder, color = Color.LightGray, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, fontSize = 14.sp) },
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

// --- 4. PREVIEW ---
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