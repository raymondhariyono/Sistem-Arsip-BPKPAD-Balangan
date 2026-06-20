package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.arsipbpkpad.presentation.components.BpkpadConfirmDialog
import com.example.arsipbpkpad.presentation.components.BpkpadTopAppBar
import com.example.arsipbpkpad.presentation.components.FormDropdownField
import com.example.arsipbpkpad.presentation.components.FormTextField
import com.example.arsipbpkpad.presentation.components.StatusDialog
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
    var showUploadConfirmDialog by remember { mutableStateOf(false) }
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionId) {
        if (sessionId.isNotEmpty()) {
            viewModel.onEvent(RapidInputUiEvent.SetCurrentSession(sessionId))
        }
    }

    if (uiState.showDuplicateWarning) {
        DuplicateWarningDialog(
            documentNumber = uiState.documentNumber,
            copyType = uiState.copyType.name,
            onConfirm = { viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick(forceSave = true)) },
            onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissDuplicateWarning) }
        )
    }

    if (showUploadConfirmDialog) {
        BpkpadConfirmDialog(
            title = stringResource(R.string.btn_push_to_db),
            message = stringResource(R.string.msg_confirm_staging),
            confirmText = stringResource(R.string.btn_confirm),
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = {
                uiState.currentSessionId?.let { viewModel.onEvent(RapidInputUiEvent.OnConfirmUpload(it)) }
                showUploadConfirmDialog = false
            },
            onDismiss = { showUploadConfirmDialog = false }
        )
    }

    if (showEditConfirmDialog) {
        BpkpadConfirmDialog(
            title = "Simpan Perubahan?",
            message = "Apakah Anda yakin ingin menyimpan perubahan pada data arsip ini?",
            confirmText = "Simpan",
            dismissText = stringResource(R.string.btn_cancel),
            onConfirm = {
                viewModel.onEvent(RapidInputUiEvent.OnSaveArchiveUpdate)
                showEditConfirmDialog = false
            },
            onDismiss = { showEditConfirmDialog = false }
        )
    }

    uiState.successMessage?.let { msg ->
        StatusDialog(
            title = stringResource(R.string.title_success),
            message = msg.asString(),
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
            message = msg.asString(),
            onDismiss = { viewModel.onEvent(RapidInputUiEvent.DismissWarning) },
            isSuccess = null
        )
    }

    if (showClassificationSheet) {
        ClassificationBottomSheet(
            uiState = uiState,
            onSearchQueryChanged = { viewModel.onEvent(RapidInputUiEvent.OnSearchQueryChanged(it)) },
            onQuickCategorySelected = { viewModel.onEvent(RapidInputUiEvent.OnQuickCategorySelected(it)) },
            onCodeSelected = { code ->
                viewModel.onEvent(RapidInputUiEvent.OnClassificationCodeChange(code))
                scope.launch { 
                    sheetState.hide() 
                    showClassificationSheet = false
                    viewModel.onEvent(RapidInputUiEvent.OnSearchQueryChanged(""))
                    viewModel.onEvent(RapidInputUiEvent.OnQuickCategorySelected(null))
                }
            },
            onDismiss = { 
                showClassificationSheet = false 
                viewModel.onEvent(RapidInputUiEvent.OnSearchQueryChanged(""))
                viewModel.onEvent(RapidInputUiEvent.OnQuickCategorySelected(null))
            },
            sheetState = sheetState
        )
    }

    Scaffold(
        topBar = {
            RapidInputTopBar(
                boxName = uiState.boxContext.box,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ScanFAB(onClick = onNavigateToScan)
        },
        bottomBar = {
            RapidInputBottomBar(
                stagedCount = uiState.stagedDocuments.size,
                isLoading = uiState.isLoading,
                onUploadClick = { 
                    if (uiState.stagedDocuments.isNotEmpty()) {
                        showUploadConfirmDialog = true
                    }
                },
                onExitClick = onNavigateBack,
                onNavigateToBottomNav = onNavigateToBottomNav
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                RapidInputForm(
                    uiState = uiState,
                    onDocTypeChange = { viewModel.onEvent(RapidInputUiEvent.OnDocTypeChange(it)) },
                    onAutoBundleToggle = { viewModel.onEvent(RapidInputUiEvent.OnAutoBundleToggle(it)) },
                    onCopyTypeChange = { viewModel.onEvent(RapidInputUiEvent.OnCopyTypeChange(it)) },
                    onCopyCountChange = { viewModel.onEvent(RapidInputUiEvent.OnCopyCountChange(it)) },
                    onDocNumberChange = { viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange(it)) },
                    onSpmDocNumberChange = { viewModel.onEvent(RapidInputUiEvent.OnSpmDocNumberChange(it)) },
                    onSubjectChange = { viewModel.onEvent(RapidInputUiEvent.OnSubjectChange(it)) },
                    onSpjDescriptionChange = { viewModel.onEvent(RapidInputUiEvent.OnSpjDescriptionChange(it)) },
                    onNominalChange = { viewModel.onEvent(RapidInputUiEvent.OnNominalChange(it)) },
                    onConditionChange = { viewModel.onEvent(RapidInputUiEvent.OnConditionChange(it)) },
                    onClassificationClick = { showClassificationSheet = true },
                    onAddOrUpdateClick = { 
                        if (uiState.editingId != null && sessionId.isEmpty()) {
                            showEditConfirmDialog = true
                        } else {
                            viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
                        }
                    },
                    onCancelEditClick = { viewModel.onEvent(RapidInputUiEvent.CancelEditing) }
                )
            }

            item {
                StagingListHeader(count = uiState.stagedDocuments.size)
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
                item { EmptyStagingState() }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun DuplicateWarningDialog(
    documentNumber: String,
    copyType: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_duplicate_warning)) },
        text = { 
            Text(stringResource(R.string.msg_duplicate_warning, documentNumber, copyType)) 
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_keep_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun RapidInputTopBar(boxName: String, onNavigateBack: () -> Unit) {
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
                    text = stringResource(R.string.title_input_box, boxName),
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
}

@Composable
fun ScanFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Scan Document")
    }
}

@Composable
fun RapidInputBottomBar(
    stagedCount: Int,
    isLoading: Boolean,
    onUploadClick: () -> Unit,
    onExitClick: () -> Unit,
    onNavigateToBottomNav: (BottomNavItem) -> Unit
) {
    Column {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (stagedCount > 0) {
                    Button(
                        onClick = onUploadClick,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Done, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.btn_upload_docs, stagedCount), 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedButton(
                    onClick = onExitClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (stagedCount == 0) stringResource(R.string.btn_finish_back) else stringResource(R.string.btn_cancel_exit),
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
}

@Composable
fun RapidInputForm(
    uiState: RapidInputUiState,
    onDocTypeChange: (DocType) -> Unit,
    onAutoBundleToggle: (Boolean) -> Unit,
    onCopyTypeChange: (DocCopyType) -> Unit,
    onCopyCountChange: (String) -> Unit,
    onDocNumberChange: (String) -> Unit,
    onSpmDocNumberChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onSpjDescriptionChange: (String) -> Unit,
    onNominalChange: (String) -> Unit,
    onConditionChange: (com.example.arsipbpkpad.domain.model.DocCondition) -> Unit,
    onClassificationClick: () -> Unit,
    onAddOrUpdateClick: () -> Unit,
    onCancelEditClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

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
                text = if (uiState.editingId != null) stringResource(R.string.title_edit_doc) else stringResource(R.string.title_add_doc),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            FormDropdownField(
                label = stringResource(R.string.label_doc_type),
                value = uiState.docType.name,
                options = listOf("SP2D", "SPM", "SPP"),
                onOptionSelected = { onDocTypeChange(DocType.valueOf(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ClassificationSelector(
                code = uiState.classificationCode,
                onClick = onClassificationClick
            )

            if (uiState.editingId == null && uiState.docType == DocType.SP2D) {
                AutoBundleCheckbox(
                    checked = uiState.isAutoBundleEnabled,
                    onCheckedChange = onAutoBundleToggle
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PhysicalStatusSelector(
                copyType = uiState.copyType,
                onCopyTypeChange = onCopyTypeChange
            )

            if (uiState.copyType == DocCopyType.COPY) {
                FormTextField(
                    label = stringResource(R.string.label_copy_count),
                    value = uiState.copyCount,
                    onValueChange = onCopyCountChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    placeholder = stringResource(R.string.placeholder_copy_count),
                    error = uiState.validationErrors["copyCount"]
                )
            }

            if (uiState.docType != DocType.SPJ || !uiState.isAutoBundleEnabled) {
                FormTextField(
                    label = if (uiState.isAutoBundleEnabled) stringResource(R.string.label_doc_number_sp2d) else stringResource(R.string.label_doc_number),
                    value = uiState.documentNumber,
                    onValueChange = onDocNumberChange,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    placeholder = stringResource(R.string.placeholder_doc_number),
                    error = uiState.validationErrors["docNumber"]
                )
            }
            
            if (uiState.isAutoBundleEnabled && uiState.editingId == null) {
                FormTextField(
                    label = stringResource(R.string.label_doc_number_spm),
                    value = uiState.spmDocumentNumber,
                    onValueChange = onSpmDocNumberChange,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    placeholder = stringResource(R.string.placeholder_spm_number),
                    error = uiState.validationErrors["spmDocNumber"]
                )
            }
            
            FormTextField(
                label = stringResource(R.string.label_description),
                value = uiState.subject,
                onValueChange = onSubjectChange,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                placeholder = stringResource(R.string.placeholder_subject),
                error = uiState.validationErrors["subject"]
            )

            if (uiState.isAutoBundleEnabled && uiState.editingId == null) {
                FormTextField(
                    label = stringResource(R.string.label_description_spj) + " (Opsional)",
                    value = uiState.spjDescription,
                    onValueChange = onSpjDescriptionChange,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    placeholder = stringResource(R.string.placeholder_spj_desc),
                    error = uiState.validationErrors["spjDescription"]
                )
            }
            
            FormTextField(
                label = stringResource(R.string.label_nominal),
                value = uiState.nominal,
                onValueChange = onNominalChange,
                visualTransformation = CurrencyVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                placeholder = stringResource(R.string.placeholder_nominal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kondisi Dokumen",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.condition == com.example.arsipbpkpad.domain.model.DocCondition.GOOD,
                    onClick = { onConditionChange(com.example.arsipbpkpad.domain.model.DocCondition.GOOD) },
                    label = { Text("Baik") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = if (uiState.condition == com.example.arsipbpkpad.domain.model.DocCondition.GOOD) {
                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = uiState.condition == com.example.arsipbpkpad.domain.model.DocCondition.DAMAGED,
                    onClick = { onConditionChange(com.example.arsipbpkpad.domain.model.DocCondition.DAMAGED) },
                    label = { Text("Rusak") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = if (uiState.condition == com.example.arsipbpkpad.domain.model.DocCondition.DAMAGED) {
                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormActionButtons(
                isEditing = uiState.editingId != null,
                onAddOrUpdateClick = onAddOrUpdateClick,
                onCancelEditClick = onCancelEditClick
            )
            
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

@Composable
fun ClassificationSelector(code: String, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = stringResource(R.string.label_classification_code),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box {
            OutlinedTextField(
                value = code,
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
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onClick() }
            )
        }
    }
}

@Composable
fun AutoBundleCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = stringResource(R.string.label_auto_bundle),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PhysicalStatusSelector(copyType: DocCopyType, onCopyTypeChange: (DocCopyType) -> Unit) {
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
                selected = copyType == DocCopyType.ORIGINAL,
                onClick = { onCopyTypeChange(DocCopyType.ORIGINAL) }
            )
            Text(stringResource(R.string.label_asli), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = copyType == DocCopyType.COPY,
                onClick = { onCopyTypeChange(DocCopyType.COPY) }
            )
            Text(stringResource(R.string.label_salinan), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FormActionButtons(isEditing: Boolean, onAddOrUpdateClick: () -> Unit, onCancelEditClick: () -> Unit) {
    if (isEditing) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelEditClick,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.btn_cancel), fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onAddOrUpdateClick,
                modifier = Modifier.weight(1f).height(56.dp),
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
            onClick = onAddOrUpdateClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
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
}

@Composable
fun StagingListHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.label_staging_list, count),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun EmptyStagingState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(48.dp), 
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationBottomSheet(
    uiState: RapidInputUiState,
    onSearchQueryChanged: (String) -> Unit,
    onQuickCategorySelected: (String?) -> Unit,
    onCodeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val filteredCodes = remember(
        uiState.searchQuery, 
        uiState.selectedQuickCategory, 
        uiState.availableCodes
    ) {
        uiState.availableCodes.filter { code ->
            val matchesCategory = uiState.selectedQuickCategory == null || 
                    code.code.startsWith(uiState.selectedQuickCategory)
            
            val matchesSearch = code.code.contains(uiState.searchQuery, ignoreCase = true) ||
                    code.name.contains(uiState.searchQuery, ignoreCase = true)
            
            matchesCategory && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDragHandle()
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

            ClassificationSearchBar(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuickCategorySection(
                categories = uiState.quickCategories,
                selectedCategoryCode = uiState.selectedQuickCategory,
                onCategorySelected = onQuickCategorySelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            ClassificationResultsList(
                codes = filteredCodes,
                isSyncing = uiState.isSyncingClassifications && uiState.availableCodes.isEmpty(),
                onCodeSelected = onCodeSelected
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BottomSheetDragHandle() {
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
}

@Composable
fun QuickCategorySection(
    categories: List<com.example.arsipbpkpad.domain.model.ClassificationCode>,
    selectedCategoryCode: String?,
    onCategorySelected: (String?) -> Unit
) {
    Text(
        text = stringResource(R.string.label_quick_category),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(8.dp))

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(categories, key = { it.code }) { category ->
            val isSelected = selectedCategoryCode == category.code
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(if (isSelected) null else category.code) },
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
}

@Composable
fun ClassificationSearchBar(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
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
}

@Composable
fun ClassificationResultsList(
    codes: List<ClassificationCode>,
    isSyncing: Boolean,
    onCodeSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isSyncing) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (codes.isEmpty()) {
            item {
                EmptyClassificationResults()
            }
        } else {
            items(codes, key = { it.code }) { classification ->
                ClassificationItem(
                    classification = classification,
                    onClick = { onCodeSelected(classification.code) }
                )
            }
        }
    }
}

@Composable
fun EmptyClassificationResults() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
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
                StagedItemHeadline(type = doc.type.name, number = doc.documentNumber)
            },
            supportingContent = { 
                StagedItemSupporting(
                    description = doc.description,
                    copyType = doc.copyType.name,
                    nominal = doc.nominal
                )
            },
            trailingContent = {
                StagedItemActions(onEdit = onEdit, onDelete = onDelete)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun StagedItemHeadline(type: String, number: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = type,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = number ?: "-", 
            fontWeight = FontWeight.Bold, 
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StagedItemSupporting(description: String?, copyType: String, nominal: Double?) {
    Column {
        Text(
            text = description ?: "-", 
            maxLines = 1, 
            fontSize = 12.sp, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Status: $copyType | Nominal: Rp ${nominal?.toLong() ?: 0}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun StagedItemActions(onEdit: () -> Unit, onDelete: () -> Unit) {
    Row {
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
    }
}
