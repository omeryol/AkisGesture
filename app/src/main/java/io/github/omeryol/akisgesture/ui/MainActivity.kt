package io.github.omeryol.akisgesture.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.github.omeryol.akisgesture.R
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.TouchApp
import io.github.omeryol.akisgesture.ui.screen.HomeScreen
import io.github.omeryol.akisgesture.ui.screen.PermissionGuideScreen
import io.github.omeryol.akisgesture.ui.screen.RuleDetailScreen
import io.github.omeryol.akisgesture.ui.screen.RuleListScreen
import io.github.omeryol.akisgesture.ui.screen.SettingsScreen
import io.github.omeryol.akisgesture.ui.component.ActionPickerScreen
import io.github.omeryol.akisgesture.ui.theme.AkisGestureTheme
import io.github.omeryol.akisgesture.ui.viewmodel.HomeViewModel
import io.github.omeryol.akisgesture.ui.viewmodel.RuleConfigViewModel
import io.github.omeryol.akisgesture.navigation.InternalNavigationBus
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AkisGestureTheme {
                AkisGestureApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AkisGestureApp() {
    val navController = rememberNavController()

    // Rules and detail pages must share one state holder. Route-scoped instances
    // can briefly show an empty list while a newly added rule is being opened.
    val ruleConfigViewModel: RuleConfigViewModel = viewModel()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    val isRulesRoute = currentRoute.startsWith("rules")
    val isRuleDetailRoute = currentRoute.startsWith("rule_detail")
    val isActionPickerRoute = currentRoute.startsWith("action_picker")
    val isMainNavigationRoute = currentRoute == "home" || isRulesRoute || currentRoute == "settings"
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel()
    val config by homeViewModel.configState.collectAsState()

    LaunchedEffect(config.hideFromRecents) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val am = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            am?.appTasks?.firstOrNull()?.setExcludeFromRecents(config.hideFromRecents)
        }
    }


    LaunchedEffect(navController, activity) {
        InternalNavigationBus.backRequests.collect {
            if (!navController.popBackStack()) {
                activity?.finish()
            }
        }
    }

    LaunchedEffect(navController) {
        InternalNavigationBus.actionPickerRequests.collect { request ->
            navController.navigate(
                "action_picker?token=${request.token}&apps=${request.appSelectionOnly}",
            )
        }
    }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isMainNavigationRoute && !isActionPickerRoute) {
                AkisGestureBottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToPermissions = {
                        navController.navigate("permissions")
                    },
                    onNavigateToRules = { edge ->
                        navController.navigate("rules?edge=${edge.name}")
                    },
                )
            }
            composable("permissions") {
                PermissionGuideScreen(
                    onAllGranted = {
                        navController.popBackStack()
                    },
                )
            }
            composable("settings") {
                val homeViewModel: HomeViewModel = viewModel()
                SettingsScreen(
                    viewModel = homeViewModel,
                )
            }
            composable(
                "rules?edge={edge}",
                arguments = listOf(navArgument("edge") {
                    type = NavType.StringType
                    defaultValue = "LEFT"
                }),
            ) { backStackEntry ->
                val edgeStr = backStackEntry.arguments?.getString("edge") ?: "LEFT"
                val initialEdge = runCatching { io.github.omeryol.akisgesture.overlay.Edge.valueOf(edgeStr) }
                    .getOrDefault(io.github.omeryol.akisgesture.overlay.Edge.LEFT)
                RuleListScreen(
                    viewModel = ruleConfigViewModel,
                    initialEdge = initialEdge,
                    onRuleClick = { ruleId ->
                        navController.navigate("rule_detail/$ruleId")
                    },
                )
            }
            composable(
                "rule_detail/{ruleId}",
                arguments = listOf(navArgument("ruleId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ruleId = backStackEntry.arguments?.getString("ruleId") ?: return@composable
                RuleDetailScreen(
                    ruleId = ruleId,
                    viewModel = ruleConfigViewModel,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(
                "action_picker?token={token}&apps={apps}",
                arguments = listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("apps") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: return@composable
                val homeViewModel: HomeViewModel = viewModel()
                val gestureConfig by homeViewModel.configState.collectAsState()
                ActionPickerScreen(
                    appSelectionOnly = backStackEntry.arguments?.getBoolean("apps") ?: false,
                    onDismiss = { navController.popBackStack() },
                    onSelect = { action ->
                        ruleConfigViewModel.onActionSelected(action)
                        InternalNavigationBus.publishActionPickerResult(
                            InternalNavigationBus.ActionPickerResult(token, action),
                        )
                        navController.popBackStack()
                    },
                    iconPack = gestureConfig.actionIconPack,
                )
            }
        }
    }
}

@Composable
private fun AkisGestureBottomBar(
    navController: NavHostController,
    currentRoute: String,
) {
    val navigationItems = listOf(
        Triple("home", stringResource(R.string.nav_home), Icons.Filled.Home),
        Triple("rules", stringResource(R.string.nav_gestures), Icons.Filled.TouchApp),
        Triple("settings", stringResource(R.string.nav_settings), Icons.Filled.Settings),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            navigationItems.forEach { (route, label, icon) ->
                val selected = when (route) {
                    "rules" -> currentRoute.startsWith("rules")
                    else -> currentRoute == route
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val destination = if (route == "rules") "rules?edge=LEFT" else route
                        if (currentRoute != route && !(route == "rules" && currentRoute.startsWith("rules"))) {
                            navController.navigate(destination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) {
                        androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    } else {
                        androidx.compose.ui.graphics.Color.Transparent
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            modifier = Modifier.size(20.dp),
                            tint = if (selected)
                                androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            label,
                            maxLines = 1,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = if (selected)
                                androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
