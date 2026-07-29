package com.omer.akisgesture.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.ui.util.actionCategories
import com.omer.akisgesture.ui.util.actionImageVector
import com.omer.akisgesture.ui.viewmodel.RuleConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionDropdownField(
    label: String,
    selected: ActionNode?,
    onSelect: (ActionNode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = { Text(selected?.label ?: "Eylem seç") },
            supportingContent = { Text(label) },
            leadingContent = selected?.let { action ->
                {
                    Icon(
                        actionImageVector(action),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            trailingContent = {
                Icon(Icons.Filled.ExpandMore, contentDescription = "Seçenekleri aç")
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            actionCategories().forEach { (category, actions) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
                actions.forEach { action ->
                    val available = RuleConfigViewModel.isActionAvailable(action)
                    DropdownMenuItem(
                        text = { Text(action.label) },
                        leadingIcon = {
                            Icon(actionImageVector(action), contentDescription = null)
                        },
                        enabled = available,
                        onClick = {
                            onSelect(action)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
