package com.omer.akisgesture.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.ui.util.actionCategories
import com.omer.akisgesture.ui.util.actionImageVector
import com.omer.akisgesture.ui.viewmodel.RuleConfigViewModel

@Composable
fun ActionPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (ActionNode) -> Unit,
) {
    val categories = actionCategories()
    var openCategory by remember { mutableStateOf(categories.firstOrNull()?.first) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Eylem seç")
                Text(
                    "Bir grubu aç, yapmak istediğin işlemi seç.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
            ) {
                categories.forEach { (category, actions) ->
                    item(key = "header_$category") {
                        ListItem(
                            headlineContent = { Text(category) },
                            supportingContent = { Text("${actions.size} seçenek") },
                            trailingContent = {
                                Icon(
                                    if (openCategory == category) Icons.Filled.ExpandLess
                                    else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                )
                            },
                            modifier = Modifier.clickable {
                                openCategory = if (openCategory == category) null else category
                            },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    if (openCategory == category) {
                        items(actions, key = { it.id }) { action ->
                            val available = RuleConfigViewModel.isActionAvailable(action)
                            ListItem(
                                headlineContent = { Text(action.label) },
                                leadingContent = {
                                    Icon(
                                        actionImageVector(action),
                                        contentDescription = null,
                                        tint = if (available) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                supportingContent = if (available) null else {
                                    { Text("Bu Android sürümünde kullanılamıyor") }
                                },
                                modifier = Modifier.clickable(enabled = available) {
                                    onSelect(action)
                                },
                                colors = androidx.compose.material3.ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                ),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        },
    )
}
