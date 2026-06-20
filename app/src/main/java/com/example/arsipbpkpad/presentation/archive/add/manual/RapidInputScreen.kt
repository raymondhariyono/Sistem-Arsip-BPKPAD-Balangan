package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ClassificationCode
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.presentation.components.BottomNavItem
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.utils.CurrencyVisualTransformation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RapidInputScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit,
    viewModel: RapidInputViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClassificationSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Ensure the session observation starts as soon as we have a sessionId from route
    LaunchedEffect(sessionId) {
        if (sessionId.isNotEmpty()) {
            viewModel.onEvent(RapidInputUiEvent.SetCurrentSession(sessionId))
        }
    }

    if (uiState.showDuplicateWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(RapidInputUiEvent.DismissDuplicateWarning) },
            title = { Text(stringResource(R.string.title_duplicate_warning)) },
            text = { 
                Text(
                    stringResource(
                        R.string.msg_duplicate_warning, 
                        uiState.documentNumber, 
                        uiState.copyType.name
                    )
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick(forceSave = true)) }
                ) {
                    Text(stringResource(R.string.btn_keep_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(RapidInputUiEvent.DismissDuplicateWarning) }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    /* Auto-navigation handled by success dialog dismissal */

    // Task 1: Success, Error, and Warning Dialogs
    uiState.successMessage?.let { msg ->
        StatusDialog(
            title = stringResource(R.string.title_success),
            message = msg,
            onDismiss = { 
                val isUpload = uiState.isUploadSuccess
                viewModel.onEvent(RapidInputUiEvent.DismissSuccess)
                if (isUpload) onNavigateBack()
            },
            isSuccess = true
        )
    }

    uiState.error?.let { msg ->
        StatusDialog(
            title = stringResource(R.string.title_error),
            message = msg,
            onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissError) },
            isSuccess = false
        )
    }

    uiState.warningMessage?.let { msg ->
        StatusDialog(
            title = stringResource(R.string.title_warning),
            message = msg,
            onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissWarning) },
            isSuccess = null // Use a different icon/color for warning
        )
    }

    if (showClassificationSheet) {
        ClassificationBottomSheet(
            uiState = uiState,
            onSearchQueryChanged = { viewModel.onEvent(RapidInputUiEvent.OnClassificationSearchQueryChanged(it)) },
            onQuickCategorySelected = { viewModel.onEvent(RapidInputUiEvent.OnQuickCategorySelected(it)) },
            onCodeSelected = { code ->
                viewModel.onEvent(RapidInputUiEvent.OnClassificationCodeChange(code))
                scope.launch { 
                    sheetState.hide() 
                    showClassificationSheet = false
                    // Reset search state on select
                    viewModel.onEvent(RapidInputUiEvent.OnClassificationSearchQueryChanged(""))
                    viewModel.onEvent(RapidInputUiEvent.OnQuickCategorySelected(null))
                }
            },
            onDismiss = { 
                showClassificationSheet = false 
                viewModel.onEvent(RapidInputUiEvent.OnClassificationSearchQueryChanged(""))
                viewModel.onEvent(RapidInputUiEvent.OnQuickCategorySelected(null))
            },
            sheetState = sheetState
        )
    }

    Scaffold(
        topBar = {
            BpkpadTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.logo_balangan),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.title_input_box, uiState.boxContext.box),
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Scan Document")
            }
        },
        bottomBar = {
            Column {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (uiState.stagedDocuments.isNotEmpty()) {
                            Button(
                                onClick = { 
                                    uiState.currentSessionId?.let { 
                                        viewModel.onEvent(RapidInputUiEvent.OnConfirmUpload(it)) 
                                    }
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Done, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.btn_upload_docs, uiState.stagedDocuments.size), 
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        OutlinedButton(
                            onClick = { onNavigateBack() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (uiState.stagedDocuments.isEmpty()) 
                                    stringResource(R.string.btn_finish_back) 
                                else 
                                    stringResource(R.string.btn_cancel_exit),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                com.example.arsipbpkpad.presentation.components.BpkpadBottomNavigation(
                    currentRoute = BottomNavItem.ADD.route,
                    onNavigate = onNavigateToBottomNav
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- TOP SECTION: FORM ---
            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (uiState.editingId != null) 
                                stringResource(R.string.title_edit_doc) 
                            else 
                                stringResource(R.string.title_add_doc),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Document Type & Bundle Logic
                        FormDropdownField(
                            label = stringResource(R.string.label_doc_type),
                            value = uiState.docType.name,
                            // Hide SPJ from manual select per rules
                            options = listOf("SP2D", "SPM", "SPP"),
                            onOptionSelected = { viewModel.onEvent(RapidInputUiEvent.OnDocTypeChange(DocType.valueOf(it))) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Classification Code
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = stringResource(R.string.label_classification_code),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box {
                                OutlinedTextField(
                                    value = uiState.classificationCode,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.UnfoldMore,
                                            contentDescription = "Pilih Kode",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                // Box overlay to make it clickable since enabled=false
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showClassificationSheet = true }
                                )
                            }
                        }

                        // Auto-Bundle Checkbox
                        if (uiState.editingId == null && uiState.docType == DocType.SP2D) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = uiState.isAutoBundleEnabled,
                                    onCheckedChange = { viewModel.onEvent(RapidInputUiEvent.OnAutoBundleToggle(it)) }
                                )
                                Text(
                                    text = stringResource(R.string.label_auto_bundle),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Physical Status (Original/Copy)
                        Text(
                            text = stringResource(R.string.label_physical_status), 
                            style = MaterialTheme.typography.labelMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = uiState.copyType == DocCopyType.ORIGINAL,
                                    onClick = { viewModel.onEvent(RapidInputUiEvent.OnCopyTypeChange(DocCopyType.ORIGINAL)) }
                                )
                                Text(stringResource(R.string.label_asli), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = uiState.copyType == DocCopyType.COPY,
                                    onClick = { viewModel.onEvent(RapidInputUiEvent.OnCopyTypeChange(DocCopyType.COPY)) }
                                )
                                Text(stringResource(R.string.label_salinan), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Copy Count (Visible only for COPY)
                        if (uiState.copyType == DocCopyType.COPY) {
                            FormTextField(
                                label = stringResource(R.string.label_copy_count),
                                value = uiState.copyCount,
                                onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnCopyCountChange(it)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = stringResource(R.string.placeholder_copy_count),
                                error = uiState.validationErrors["copyCount"]
                            )
                        }

                        if (uiState.docType != DocType.SPJ || !uiState.isAutoBundleEnabled) {
                            FormTextField(
                                label = if (uiState.isAutoBundleEnabled) 
                                    stringResource(R.string.label_doc_number_sp2d) 
                                else 
                                    stringResource(R.string.label_doc_number),
                                value = uiState.documentNumber,
                                onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange(it)) },
                                placeholder = stringResource(R.string.placeholder_doc_number),
                                error = uiState.validationErrors["docNumber"]
                            )
                        }
                        
                        if (uiState.isAutoBundleEnabled && uiState.editingId == null) {
                            FormTextField(
                                label = stringResource(R.string.label_doc_number_spm),
                                value = uiState.spmDocumentNumber,
                                onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnSpmDocNumberChange(it)) },
                                placeholder = stringResource(R.string.placeholder_spm_number),
                                error = uiState.validationErrors["spmDocNumber"]
                            )
                        }
                        
                        FormTextField(
                            label = stringResource(R.string.label_description),
                            value = uiState.subject,
                            onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnSubjectChange(it)) },
                            placeholder = stringResource(R.string.placeholder_subject),
                            error = uiState.validationErrors["subject"]
                        )

                        if (uiState.isAutoBundleEnabled && uiState.editingId == null) {
                            FormTextField(
                                label = stringResource(R.string.label_description_spj),
                                value = uiState.spjDescription,
                                onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnSpjDescriptionChange(it)) },
                                placeholder = stringResource(R.string.placeholder_spj_desc),
                                error = uiState.validationErrors["spjDescription"]
                            )
                        }
                        
                        FormTextField(
                            label = stringResource(R.string.label_nominal),
                            value = uiState.nominal,
                            onValueChange = { viewModel.onEvent(RapidInputUiEvent.OnNominalChange(it)) },
                            visualTransformation = CurrencyVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = stringResource(R.string.placeholder_nominal)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.editingId != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.onEvent(RapidInputUiEvent.CancelEditing) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(R.string.btn_cancel), fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick()) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.btn_update),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.btn_add_to_staging),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        uiState.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // --- SECTION HEADER ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_staging_list, uiState.stagedDocuments.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            items(uiState.stagedDocuments, key = { it.id }) { doc ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    StagedItemRow(
                        doc = doc,
                        onDelete = { viewModel.onEvent(RapidInputUiEvent.OnDeleteStagedDoc(doc.id)) },
                        onEdit = { viewModel.onEvent(RapidInputUiEvent.OnEditStagedDoc(doc)) }
                    )
                }
            }
            
            if (uiState.stagedDocuments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.msg_empty_staging), 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationBottomSheet(
    uiState: RapidInputUiState,
    onSearchQueryChanged: (String) -> Unit,
    onQuickCategorySelected: (ClassificationCode?) -> Unit,
    onCodeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val filteredCodes = remember(
        uiState.classificationSearchQuery, 
        uiState.selectedQuickCategory, 
        uiState.availableCodes
    ) {
        uiState.availableCodes.filter { code ->
            val matchesCategory = uiState.selectedQuickCategory == null || 
                    code.code.startsWith(uiState.selectedQuickCategory.code)
            
            val matchesSearch = code.code.contains(uiState.classificationSearchQuery, ignoreCase = true) ||
                    code.name.contains(uiState.classificationSearchQuery, ignoreCase = true)
            
            matchesCategory && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp, 4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.title_select_classification),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kategori Cepat (Filter Chips) - Moved above Search Bar for better reach
            Text(
                text = stringResource(R.string.label_quick_category),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.quickCategories.forEach { category ->
                    val isSelected = uiState.selectedQuickCategory?.code == category.code
                    FilterChip(
                        selected = isSelected,
                        onClick = { onQuickCategorySelected(category) },
                        label = { 
                            Text(
                                text = "${category.code} ${category.name}",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            ) 
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.classificationSearchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_classification_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isSyncingClassifications && uiState.availableCodes.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (filteredCodes.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.msg_no_classification_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredCodes, key = { it.code }) { classification ->
                        ClassificationItem(
                            classification = classification,
                            onClick = { onCodeSelected(classification.code) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ClassificationItem(
    classification: ClassificationCode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = classification.code,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                if (classification.parentCode != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_parent_code, classification.parentCode),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = classification.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StagedItemRow(
    doc: ArchiveDocument,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        ListItem(
            headlineContent = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = doc.type.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = doc.documentNumber ?: "-", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            supportingContent = { 
                Column {
                    Text(
                        text = doc.description ?: "-", 
                        maxLines = 1, 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Status: ${doc.copyType} | Nominal: Rp ${doc.nominal?.toLong() ?: 0}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun StatusDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    isSuccess: Boolean? = true // true: success, false: error, null: warning
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when (isSuccess) {
                    true -> Icons.Default.CheckCircle
                    false -> Icons.Default.Cancel
                    else -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = when (isSuccess) {
                    true -> Color(0xFF4CAF50)
                    false -> MaterialTheme.colorScheme.error
                    else -> Color(0xFFFF9800)
                },
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text(stringResource(R.string.btn_ok))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String? = null
) {
    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            placeholder = { 
                Text(
                    text = placeholder ?: stringResource(R.string.hint_enter_value, label), 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ) 
            },
            supportingText = error?.let { { Text(it, style = MaterialTheme.typography.labelSmall) } },
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
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
                        stringResource(R.string.hint_select_value, label), 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ) 
                },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White // Enforce white background for dropdown
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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
