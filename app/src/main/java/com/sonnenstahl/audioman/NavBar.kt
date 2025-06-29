package com.sonnenstahl.audioman

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sonnenstahl.audioman.ui.theme.LightTeal
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.Router

@Composable
fun NavBar(
    navController: NavController,
    routes: List<Router>,
) {
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = currentBackStackEntry?.destination?.route
    val darkMode = isSystemInDarkTheme()

    NavigationBar {
        routes.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                    )
                },
                label = {
                    Text(screen.label)
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = if (darkMode) Teal else LightTeal,
                        selectedTextColor = if (darkMode) Teal else LightTeal,
                        indicatorColor = Color.Transparent,
                    ),
            )
        }
    }
}
