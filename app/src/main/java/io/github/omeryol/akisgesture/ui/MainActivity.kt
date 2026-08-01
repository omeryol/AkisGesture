package io.github.omeryol.akisgesture.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
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
import io.github.omeryol.akisgesture.ui.theme.AkisGestureTheme
import io.github.omeryol.akisgesture.ui.viewmodel.HomeViewModel
import io.github.omeryol.akisgesture.ui.viewmodel.RuleConfigViewModel
import io.github.omeryol.akisgesture.navigation.InternalNavigationBus
import android.app.Activity

class MainActivity : ComponentActivity() {

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
    val isMainNavigationRoute = currentRoute == "home" || isRulesRoute || currentRoute == "settings"
    val activity = LocalContext.current as? Activity

    LaunchedEffect(navController, activity) {
        InternalNavigationBus.backRequests.collect {
            if (!navController.popBackStack()) {
                activity?.finish()
            }
        }
    }

    Scaffold(
        topBar = {
            if (!isRulesRoute && !isRuleDetailRoute) {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                currentRoute == "home" -> "Akış Gesture"
                                currentRoute == "permissions" -> "İzinler"
                                currentRoute == "settings" -> "Ayarlar"
                                isRulesRoute -> "Hareketler"
                                else -> "Akış Gesture"
                            }
                        )
                    },
                )
            }
        },
        bottomBar = {
            if (isMainNavigationRoute) {
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
        }
    }
}

@Composable
private fun AkisGestureBottomBar(
    navController: NavHostController,
    currentRoute: String,
) {
    val navigationItems = listOf(
        Triple("home", "Ana ekran", Icons.Filled.Home),
        Triple("rules", "Hareketler", Icons.Filled.TouchApp),
        Triple("settings", "Ayarlar", Icons.Filled.Settings),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = androidx.compose.ui.graphics.Color(0xF2181B2B),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(
            1.dp,
            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f),
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
                        if (!selected) {
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) {
                        androidx.compose.ui.graphics.Color(0xFF3346A8).copy(alpha = 0.72f)
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
                                androidx.compose.ui.graphics.Color(0xFF8DF5FF)
                            else
                                androidx.compose.ui.graphics.Color(0xFFA8ADC4),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            label,
                            maxLines = 1,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = if (selected)
                                androidx.compose.ui.graphics.Color.White
                            else
                                androidx.compose.ui.graphics.Color(0xFFA8ADC4),
                        )
                    }
                }
            }
        }
    }
}
