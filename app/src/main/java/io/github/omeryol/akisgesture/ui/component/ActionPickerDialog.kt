package io.github.omeryol.akisgesture.ui.component

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import io.github.omeryol.akisgesture.R
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.ui.util.actionCategories
import io.github.omeryol.akisgesture.ui.util.actionEmoji
import io.github.omeryol.akisgesture.ui.util.filterActions
import io.github.omeryol.akisgesture.ui.util.localizedLabel
import io.github.omeryol.akisgesture.ui.viewmodel.RuleConfigViewModel
import java.text.Collator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)
@Composable
fun ActionPickerScreen(
    onDismiss: () -> Unit,
    onSelect: (ActionNode) -> Unit,
    appSelectionOnly: Boolean = false,
    iconPack: ActionIconPack = ActionIconPack.EMOJI_MODERN,
) {
    val context = LocalContext.current
    val categories = actionCategories()
    val frequentActions = remember {
        listOf(
            ActionNode.Back,
            ActionNode.Home,
            ActionNode.Recents,
            ActionNode.SwitchLastApp,
            ActionNode.ForceStopForeground,
        )
    }
    val keyActions = remember {
        listOf(
            ActionNode.SendKeyCode(KeyEvent.KEYCODE_BACK, context.getString(R.string.key_back)),
            ActionNode.SendKeyCode(KeyEvent.KEYCODE_HOME, context.getString(R.string.key_home)),
            ActionNode.SendKeyCode(KeyEvent.KEYCODE_APP_SWITCH, context.getString(R.string.key_recents)),
            ActionNode.SendKeyCode(KeyEvent.KEYCODE_VOLUME_UP, context.getString(R.string.key_volume_up)),
            ActionNode.SendKeyCode(KeyEvent.KEYCODE_VOLUME_DOWN, context.getString(R.string.key_volume_down)),
            ActionNode.SendKeyCode(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, context.getString(R.string.key_media_toggle)),
        )
    }
    val categoryMap = remember(categories) { categories.toMap() }
    val pickerCategories = remember(categories, keyActions, frequentActions) {
        listOf(
            "Sık" to frequentActions,
            "Gezinme" to categoryMap["Gezinme"].orEmpty(),
            "Sistem" to listOf(
                "Sistem", "Paneller", "Asistan", "Ekran", "Sistem Arayüzü", "Root",
            ).flatMap { categoryMap[it].orEmpty() },
            "Medya & Araçlar" to listOf(
                "Medya", "Döndürme", "Donanım", "Diğer",
            ).flatMap { categoryMap[it].orEmpty() } + keyActions,
        )
    }
    val fixedActions = remember(pickerCategories) {
        pickerCategories.flatMap { it.second }.distinctBy { it.id }
    }
    var installedApps by remember { mutableStateOf<List<ActionNode.LaunchApp>>(emptyList()) }
    var appsLoaded by remember { mutableStateOf(false) }
    var browsingApps by remember(appSelectionOnly) { mutableStateOf(appSelectionOnly) }
    var selectedCategory by remember { mutableStateOf(pickerCategories.firstOrNull()?.first) }
    var query by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val allSearchableActions = remember(fixedActions, installedApps) {
        fixedActions + installedApps
    }
    val searchResults = remember(allSearchableActions, query, browsingApps) {
        filterActions(
            if (browsingApps) installedApps else allSearchableActions,
            query,
            { action -> action.localizedLabel(context) },
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
            val collator = Collator.getInstance(context.resources.configuration.locales[0])
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

    Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    stringResource(if (browsingApps) R.string.choose_app else R.string.choose_action),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                )
                                Text(
                                    stringResource(if (browsingApps) R.string.choose_app_hint else R.string.choose_action_hint),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                },
            ) { innerPadding ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
                ) {
                if (browsingApps && !appSelectionOnly) {
                    item(key = "back_to_actions") {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.back_to_actions)) },
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
                            Text(stringResource(if (browsingApps) R.string.search_apps else R.string.search_actions))
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = if (query.isNotEmpty()) {
                            {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = stringResource(R.string.clear_search),
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
                            EmptyResult(stringResource(R.string.no_matching_app))
                        }
                    } else {
                        item(key = "app_grid") {
                            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                                searchResults.chunked(3).forEach { rowApps ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                                    ) {
                                        rowApps.forEach { action ->
                                            ActionPickerItem(
                                                action = action,
                                                onSelect = onSelect,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        repeat(3 - rowApps.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (query.isNotBlank()) {
                    if (searchResults.isEmpty()) {
                        item(key = "empty_search") {
                            EmptyResult(stringResource(R.string.no_matching_action))
                        }
                    } else {
                        items(searchResults, key = { "search_${it.id}" }) { action ->
                            ActionPickerItem(action = action, onSelect = onSelect)
                        }
                    }
                } else {
                    item(key = "launch_app") {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.launch_app)) },
                            supportingContent = {
                                Text(
                                    if (appsLoaded) stringResource(R.string.choose_from_apps, installedApps.size)
                                    else stringResource(R.string.apps_loading),
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                    item(key = "category_tabs") {
                        FlowRow(
                            maxItemsInEachRow = 4,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            pickerCategories.forEachIndexed { index, (category, actions) ->
                                val accent = pickerCategoryColor(index)
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            pickerCategoryLabel(category),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                        )
                                    },
                                    trailingIcon = { Text("${actions.size}", style = MaterialTheme.typography.labelSmall) },
                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accent.copy(alpha = 0.24f),
                                        selectedLabelColor = accent,
                                        selectedTrailingIconColor = accent,
                                        containerColor = accent.copy(alpha = 0.08f),
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        accent.copy(alpha = if (selectedCategory == category) 0.85f else 0.28f),
                                    ),
                                )
                            }
                        }
                    }
                    val selectedActions = pickerCategories
                        .firstOrNull { it.first == selectedCategory }
                        ?.second
                        .orEmpty()
                    item(key = "category_actions") {
                        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                            selectedActions.chunked(3).forEach { rowActions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                                ) {
                                    rowActions.forEach { action ->
                                        ActionPickerItem(
                                            action = action,
                                            onSelect = onSelect,
                                            modifier = Modifier.weight(1f),
                                            accentColor = pickerCategoryColor(
                                                pickerCategories.indexOfFirst { it.first == selectedCategory },
                                            ),
                                            iconPack = iconPack,
                                        )
                                    }
                                    if (rowActions.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
    }
}
}

@Composable
private fun pickerCategoryLabel(category: String): String = when (category) {
    "Sık" -> stringResource(R.string.category_frequent)
    "Gezinme" -> stringResource(R.string.category_navigation)
    "Sistem" -> stringResource(R.string.category_system)
    "Medya & Araçlar" -> stringResource(R.string.category_media_tools)
    else -> category
}

private fun pickerCategoryColor(index: Int): Color = listOf(
    Color(0xFF5B8CFF),
    Color(0xFFFF6B6B),
    Color(0xFFFFB74D),
    Color(0xFF4DD0E1),
    Color(0xFFB39DDB),
    Color(0xFF66BB6A),
    Color(0xFFFF8A65),
    Color(0xFF26C6DA),
    Color(0xFFEF5350),
    Color(0xFF90A4AE),
).getOrElse(index % 10) { Color(0xFF5B8CFF) }

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
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    iconPack: ActionIconPack = ActionIconPack.EMOJI_MODERN,
) {
    val context = LocalContext.current
    val available = RuleConfigViewModel.isActionAvailable(action)
    val scheme = MaterialTheme.colorScheme
    val accent = accentColor ?: scheme.primary
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = 112.dp, max = 132.dp)
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = if (available) 0.15f else 0.07f))
            .border(
                1.dp,
                if (available) accent.copy(alpha = 0.42f)
                else scheme.outlineVariant.copy(alpha = 0.12f),
                RoundedCornerShape(14.dp),
            )
            .clickable(enabled = available) { onSelect(action) }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (available) accent.copy(alpha = 0.24f)
                    else scheme.onSurfaceVariant.copy(alpha = 0.10f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            ActionIcon(
                action = action,
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                tint = if (available) accent else scheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${actionEmoji(action, iconPack)} ${action.localizedLabel(context)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = if (available) scheme.onSurface else scheme.onSurfaceVariant,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                when {
                    !available -> stringResource(R.string.unavailable_android)
                    action is ActionNode.LaunchApp -> stringResource(R.string.launch_app)
                    else -> stringResource(R.string.gesture_action)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (available) accent else scheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        if (!available) {
            Text(stringResource(R.string.disabled), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        } else {
            Text(stringResource(R.string.select), style = MaterialTheme.typography.labelSmall, color = accent)
        }
    }
}
