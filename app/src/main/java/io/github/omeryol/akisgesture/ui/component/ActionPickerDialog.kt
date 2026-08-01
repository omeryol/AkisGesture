package io.github.omeryol.akisgesture.ui.component

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    var openCategory by remember { mutableStateOf(categories.firstOrNull()?.first) }
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
        containerColor = Color(0xEE161827),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(if (browsingApps) "Uygulama seç" else "Eylem seç")
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
    ListItem(
        headlineContent = { Text(action.label) },
        supportingContent = when {
            !available -> {
                { Text("Bu Android sürümünde kullanılamıyor") }
            }
            action is ActionNode.LaunchApp -> {
                { Text("Uygulamayı aç") }
            }
            else -> null
        },
        leadingContent = {
            ActionIcon(
                action = action,
                contentDescription = null,
                modifier = if (action is ActionNode.LaunchApp) Modifier.size(32.dp) else Modifier,
                tint = if (available) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier.clickable(enabled = available) {
            onSelect(action)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
