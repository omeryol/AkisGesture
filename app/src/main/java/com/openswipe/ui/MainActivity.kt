package com.omer.akisgesture.ui

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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Edit
import com.omer.akisgesture.ui.screen.HomeScreen
import com.omer.akisgesture.ui.screen.PermissionGuideScreen
import com.omer.akisgesture.ui.screen.RuleDetailScreen
import com.omer.akisgesture.ui.screen.RuleListScreen
import com.omer.akisgesture.ui.screen.SettingsScreen
import com.omer.akisgesture.ui.theme.AkisGestureTheme
import com.omer.akisgesture.ui.viewmodel.HomeViewModel
import com.omer.akisgesture.ui.viewmodel.RuleConfigViewModel
import com.omer.akisgesture.navigation.InternalNavigationBus
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
            composable("rules") {
                RuleListScreen(
                    viewModel = ruleConfigViewModel,
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
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Ana ekran") },
            label = { Text("Ana ekran") },
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Edit, contentDescription = "Kurallar") },
            label = { Text("Kurallar") },
            selected = currentRoute == "rules",
            onClick = {
                if (currentRoute != "rules") {
                    navController.navigate("rules") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Ayarlar") },
            label = { Text("Ayarlar") },
            selected = currentRoute == "settings",
            onClick = {
                if (currentRoute != "settings") {
                    navController.navigate("settings") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )
    }
}
