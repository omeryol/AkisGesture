package io.github.omeryol.akisgesture.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
            if (currentRoute != "rules" && !currentRoute.startsWith("rule_detail")) {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                currentRoute == "home" -> "Akış Gesture"
                                currentRoute == "permissions" -> "İzinler"
                                currentRoute == "settings" -> "Ayarlar"
                                else -> "Akış Gesture"
                            }
                        )
                    },
                )
            }
        },
        bottomBar = {
            if (currentRoute in listOf("home", "rules", "settings")) {
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
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color(0xEE121422),
        tonalElevation = 8.dp,
    ) {
        val items = listOf(
            Triple("home", "Ana ekran", Icons.Filled.Home),
            Triple("rules", "Hareketler", Icons.Filled.TouchApp),
            Triple("settings", "Ayarlar", Icons.Filled.Settings),
        )
        items.forEach { (route, label, icon) ->
            val selected = currentRoute == route
            NavigationBarItem(
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = if (selected)
                            androidx.compose.ui.graphics.Color(0xFF00E5FF)
                        else
                            androidx.compose.ui.graphics.Color(0xFF8E92B0),
                    )
                },
                label = {
                    Text(
                        label,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = if (selected)
                            androidx.compose.ui.graphics.Color(0xFF00E5FF)
                        else
                            androidx.compose.ui.graphics.Color(0xFF8E92B0),
                    )
                },
                selected = selected,
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFF3D5AFE).copy(alpha = 0.28f),
                    selectedIconColor = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                    unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF8E92B0),
                    selectedTextColor = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                    unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF8E92B0),
                ),
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            if (route == "home") {
                                popUpTo("home") { inclusive = true }
                            } else {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
            )
        }
    }
}
