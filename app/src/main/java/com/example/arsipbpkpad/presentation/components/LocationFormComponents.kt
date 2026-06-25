package com.example.arsipbpkpad.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arsipbpkpad.domain.model.Room
import com.example.arsipbpkpad.domain.model.Shelf
import com.example.arsipbpkpad.utils.ResultState

@Composable
fun HierarchicalLocationSelector(
    roomsList: ResultState<List<Room>>,
    shelvesList: ResultState<List<Shelf>>,
    selectedRoom: Room?,
    selectedShelf: Shelf?,
    typedRoom: String,
    typedShelf: String,
    typedBox: String,
    onRoomChange: (String) -> Unit,
    onRoomSelected: (Room?) -> Unit,
    onCreateRoom: (String) -> Unit,
    onShelfChange: (String) -> Unit,
    onShelfSelected: (Shelf?) -> Unit,
    onCreateShelf: (String) -> Unit,
    onBoxChange: (String) -> Unit,
    boxError: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LocationDropdown(
            label = "Gudang",
            value = typedRoom,
            options = (roomsList as? ResultState.Success)?.data ?: emptyList(),
            onValueChange = onRoomChange,
            onOptionSelected = { onRoomSelected(it) },
            onCreateNew = { onCreateRoom(it) },
            getItemName = { it.name },
            enabled = true
        )

        LocationDropdown(
            label = "Rak",
            value = typedShelf,
            options = (shelvesList as? ResultState.Success)?.data ?: emptyList(),
            onValueChange = onShelfChange,
            onOptionSelected = { onShelfSelected(it) },
            onCreateNew = { onCreateShelf(it) },
            getItemName = { it.name },
            enabled = selectedRoom != null
        )

        FormTextField(
            label = "Box",
            value = typedBox,
            onValueChange = onBoxChange,
            enabled = selectedShelf != null,
            placeholder = "Contoh: B-101",
            error = boxError
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LocationDropdown(
    label: String,
    value: String,
    options: List<T>,
    onValueChange: (String) -> Unit,
    onOptionSelected: (T) -> Unit,
    onCreateNew: (String) -> Unit,
    getItemName: (T) -> String,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    expanded = true
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth(),
                enabled = enabled,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
                )
            )

            val filteredOptions = options.filter { getItemName(it).contains(value, ignoreCase = true) }
            val showAddNew = value.isNotBlank() && filteredOptions.none { getItemName(it).equals(value, ignoreCase = true) }

            ExposedDropdownMenu(
                expanded = expanded && enabled && (filteredOptions.isNotEmpty() || showAddNew),
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(getItemName(option)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
                if (showAddNew) {
                    DropdownMenuItem(
                        text = { Text("+ Tambah '$value'", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onCreateNew(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
