package io.github.omeryol.akisgesture.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
import androidx.compose.runtime.CompositionLocalProvider
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
import io.github.omeryol.akisgesture.ui.screen.HomeScreen
import io.github.omeryol.akisgesture.ui.screen.PermissionGuideScreen
import io.github.omeryol.akisgesture.ui.screen.RuleDetailScreen
import io.github.omeryol.akisgesture.ui.screen.RuleListScreen
import io.github.omeryol.akisgesture.ui.screen.SettingsScreen
import io.github.omeryol.akisgesture.ui.component.ActionPickerScreen
import io.github.omeryol.akisgesture.ui.component.AkisFlowGlyph
import io.github.omeryol.akisgesture.ui.component.AkisFlowGlyphIcon
import io.github.omeryol.akisgesture.ui.component.LocalActionIconColorMode
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
    val isMainNavigationRoute = currentRoute == "home" || isRulesRoute || currentRoute.startsWith("settings")
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

    CompositionLocalProvider(LocalActionIconColorMode provides config.actionIconColorMode) {
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
                .then(
                    if (isRulesRoute) {
                        Modifier
                            .padding(innerPadding)
                            .offset(y = (-24).dp)
                    } else {
                        Modifier.padding(innerPadding)
                    },
                ),
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
            composable(
                "settings?section={section}",
                arguments = listOf(navArgument("section") {
                    type = NavType.IntType
                    defaultValue = 0
                }),
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = viewModel()
                SettingsScreen(
                    viewModel = homeViewModel,
                    initialSection = backStackEntry.arguments?.getInt("section") ?: 0,
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
                    onNavigateToSettings = { section ->
                        navController.navigate("settings?section=$section")
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
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
private fun AkisGestureBottomBar(
    navController: NavHostController,
    currentRoute: String,
) {
    val navigationItems = listOf(
        NavigationItem(
            route = "home",
            label = stringResource(R.string.nav_home),
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        NavigationItem(
            route = "rules",
            label = stringResource(R.string.nav_gestures),
            selectedIcon = Icons.Filled.Gesture,
            unselectedIcon = Icons.Outlined.Gesture,
        ),
        NavigationItem(
            route = "settings",
            label = stringResource(R.string.nav_settings),
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        ),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
        border = BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationItems.forEach { item ->
                val selected = when (item.route) {
                    "rules" -> currentRoute.startsWith("rules")
                    else -> currentRoute.startsWith(item.route)
                }

                val activeBackgroundColor by animateColorAsState(
                    targetValue = if (selected) {
                        androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "navBgColor",
                )

                val activeContentColor by animateColorAsState(
                    targetValue = if (selected) {
                        androidx.compose.material3.MaterialTheme.colorScheme.primary
                    } else {
                        androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "navContentColor",
                )

                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val destination = if (item.route == "rules") "rules?edge=LEFT" else if (item.route == "settings") "settings?section=0" else item.route
                        if (currentRoute != item.route && !(item.route == "rules" && currentRoute.startsWith("rules"))) {
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
                    color = activeBackgroundColor,
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = activeContentColor,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = activeContentColor,
                        )
                    }
                }
            }
        }
    }
}
