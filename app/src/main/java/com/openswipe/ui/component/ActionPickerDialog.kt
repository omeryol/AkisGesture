package com.omer.akisgesture.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import java.util.Locale

@Composable
fun ActionPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (ActionNode) -> Unit,
) {
    val categories = actionCategories()
    var openCategory by remember { mutableStateOf(categories.firstOrNull()?.first) }
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.trim().lowercase(Locale("tr", "TR"))
    val searchResults = if (normalizedQuery.isBlank()) {
        emptyList()
    } else {
        categories
            .flatMap { it.second }
            .distinctBy { it.id }
            .filter { it.label.lowercase(Locale("tr", "TR")).contains(normalizedQuery) }
    }
    val frequentActions = listOf(
        ActionNode.Back,
        ActionNode.Home,
        ActionNode.Recents,
        ActionNode.SwitchLastApp,
        ActionNode.ForceStopForeground,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Eylem seç")
                Text(
                    "Ara veya sık kullanılanlardan birini seç.",
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
                item(key = "search") {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        label = { Text("Eylem ara") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = if (query.isNotEmpty()) {
                            {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Aramayı temizle",
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )
                }

                if (normalizedQuery.isNotBlank()) {
                    if (searchResults.isEmpty()) {
                        item(key = "empty_search") {
                            Text(
                                "Eşleşen eylem bulunamadı",
                                modifier = Modifier.padding(vertical = 18.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(searchResults, key = { "search_${it.id}" }) { action ->
                            ActionPickerItem(action = action, onSelect = onSelect)
                        }
                    }
                } else {
                    item(key = "frequent_header") {
                        Text(
                            "Sık kullanılanlar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(frequentActions, key = { "frequent_${it.id}" }) { action ->
                        ActionPickerItem(action = action, onSelect = onSelect)
                    }

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
                                    openCategory =
                                        if (openCategory == category) null else category
                                },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        if (openCategory == category) {
                            items(actions, key = { it.id }) { action ->
                                ActionPickerItem(action = action, onSelect = onSelect)
                            }
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

@Composable
private fun ActionPickerItem(
    action: ActionNode,
    onSelect: (ActionNode) -> Unit,
) {
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
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
