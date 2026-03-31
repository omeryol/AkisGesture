package com.openswipe.ui

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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openswipe.ui.screen.HomeScreen
import com.openswipe.ui.screen.PermissionGuideScreen
import com.openswipe.ui.theme.OpenSwipeTheme
import com.openswipe.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenSwipeTheme {
                OpenSwipeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenSwipeApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            "home" -> "OpenSwipe"
                            "permissions" -> "权限设置"
                            else -> "OpenSwipe"
                        }
                    )
                },
            )
        },
        bottomBar = {
            if (currentRoute != "permissions") {
                OpenSwipeBottomBar(navController = navController, currentRoute = currentRoute)
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
        }
    }
}

@Composable
private fun OpenSwipeBottomBar(
    navController: NavHostController,
    currentRoute: String,
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "主页") },
            label = { Text("主页") },
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
            icon = { Icon(Icons.Filled.Settings, contentDescription = "设置") },
            label = { Text("设置") },
            selected = currentRoute == "settings",
            onClick = {
                // Phase 2: 设置页面
            },
        )
    }
}
