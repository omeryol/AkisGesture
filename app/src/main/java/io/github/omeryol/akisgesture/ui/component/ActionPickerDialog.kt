package io.github.omeryol.akisgesture.ui.component

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.ui.util.actionCategories
import io.github.omeryol.akisgesture.ui.util.filterActions
import io.github.omeryol.akisgesture.ui.viewmodel.RuleConfigViewModel
import java.text.Collator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ActionPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (ActionNode) -> Unit,
    appSelectionOnly: Boolean = false,
) {
    val context = LocalContext.current
    val categories = actionCategories()
    val fixedActions = remember(categories) {
        categories.flatMap { it.second }.distinctBy { it.id }
    }
    var installedApps by remember { mutableStateOf<List<ActionNode.LaunchApp>>(emptyList()) }
    var appsLoaded by remember { mutableStateOf(false) }
    var browsingApps by remember(appSelectionOnly) { mutableStateOf(appSelectionOnly) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.first) }
    var query by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val allSearchableActions = remember(fixedActions, installedApps) {
        fixedActions + installedApps
    }
    val searchResults = remember(allSearchableActions, query, browsingApps) {
        filterActions(
            if (browsingApps) installedApps else allSearchableActions,
            query,
        )
    }
    val frequentActions = remember {
        listOf(
            ActionNode.Back,
            ActionNode.Home,
            ActionNode.Recents,
            ActionNode.SwitchLastApp,
            ActionNode.ForceStopForeground,
        )
    }

    LaunchedEffect(context.packageName) {
        installedApps = withContext(Dispatchers.IO) {
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val packageManager = context.packageManager
            val resolved = if (Build.VERSION.SDK_INT >= 33) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launcherIntent, 0)
            }
            val collator = Collator.getInstance(java.util.Locale("tr", "TR"))
            resolved
                .mapNotNull { info ->
                    val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                    if (packageName == context.packageName) return@mapNotNull null
                    val label = info.loadLabel(packageManager).toString().trim()
                    if (label.isBlank()) return@mapNotNull null
                    ActionNode.LaunchApp(packageName, label)
                }
                .distinctBy { it.packageName }
                .sortedWith { first, second -> collator.compare(first.appName, second.appName) }
        }
        appsLoaded = true
    }
    LaunchedEffect(browsingApps) {
        listState.scrollToItem(0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(
                    if (browsingApps) "Uygulama seç" else "Eylem seç",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    if (browsingApps) {
                        "Hareketle açmak istediğin uygulamayı seç."
                    } else {
                        "Ara veya sık kullanılanlardan birini seç."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
            ) {
                if (browsingApps && !appSelectionOnly) {
                    item(key = "back_to_actions") {
                        ListItem(
                            headlineContent = { Text("Tüm eylemlere dön") },
                            leadingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            },
                            modifier = Modifier.clickable {
                                browsingApps = false
                                query = ""
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }

                item(key = "search") {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        label = {
                            Text(if (browsingApps) "Uygulama ara" else "Eylem veya uygulama ara")
                        },
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

                if (browsingApps) {
                    if (!appsLoaded) {
                        item(key = "loading_apps") {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(20.dp),
                            )
                        }
                    } else if (searchResults.isEmpty()) {
                        item(key = "empty_apps") {
                            EmptyResult("Eşleşen uygulama bulunamadı")
                        }
                    } else {
                        items(searchResults, key = { "app_${it.id}" }) { action ->
                            ActionPickerItem(action = action, onSelect = onSelect)
                        }
                    }
                } else if (query.isNotBlank()) {
                    if (searchResults.isEmpty()) {
                        item(key = "empty_search") {
                            EmptyResult("Eşleşen eylem veya uygulama bulunamadı")
                        }
                    } else {
                        items(searchResults, key = { "search_${it.id}" }) { action ->
                            ActionPickerItem(action = action, onSelect = onSelect)
                        }
                    }
                } else {
                    item(key = "launch_app") {
                        ListItem(
                            headlineContent = { Text("Uygulama aç") },
                            supportingContent = {
                                Text(
                                    if (appsLoaded) "${installedApps.size} uygulamadan seç"
                                    else "Uygulamalar hazırlanıyor",
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            trailingContent = {
                                Icon(Icons.Filled.ExpandMore, contentDescription = null)
                            },
                            modifier = Modifier.clickable { browsingApps = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
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

                    item(key = "category_tabs") {
                        LazyRow(
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            items(categories, key = { "tab_${it.first}" }) { (category, actions) ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) },
                                    trailingIcon = { Text("${actions.size}") },
                                )
                            }
                        }
                    }
                    val selectedActions = categories
                        .firstOrNull { it.first == selectedCategory }
                        ?.second
                        .orEmpty()
                    items(selectedActions, key = { "category_${it.id}" }) { action ->
                        ActionPickerItem(action = action, onSelect = onSelect)
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
private fun EmptyResult(message: String) {
    Text(
        message,
        modifier = Modifier.padding(vertical = 18.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ActionPickerItem(
    action: ActionNode,
    onSelect: (ActionNode) -> Unit,
) {
    val available = RuleConfigViewModel.isActionAvailable(action)
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.surfaceVariant.copy(alpha = if (available) 0.32f else 0.16f))
            .border(
                1.dp,
                if (available) scheme.outlineVariant.copy(alpha = 0.28f)
                else scheme.outlineVariant.copy(alpha = 0.12f),
                RoundedCornerShape(14.dp),
            )
            .clickable(enabled = available) { onSelect(action) }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (available) scheme.primary.copy(alpha = 0.16f)
                    else scheme.onSurfaceVariant.copy(alpha = 0.10f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            ActionIcon(
                action = action,
                contentDescription = null,
                modifier = Modifier.size(27.dp),
                tint = if (available) scheme.primary else scheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                action.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = if (available) scheme.onSurface else scheme.onSurfaceVariant,
            )
            Text(
                when {
                    !available -> "Bu Android sürümünde kullanılamıyor"
                    action is ActionNode.LaunchApp -> "Uygulamayı aç"
                    else -> "Hareket eylemi"
                },
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
        if (!available) {
            Text("Kapalı", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        } else {
            Text("Seç", style = MaterialTheme.typography.labelSmall, color = scheme.primary)
        }
    }
}
