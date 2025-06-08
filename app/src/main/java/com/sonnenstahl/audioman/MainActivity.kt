package com.sonnenstahl.audioman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sonnenstahl.audioman.ui.theme.AudioManTheme
import com.sonnenstahl.audioman.utils.Router

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioManTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStack = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack.value?.destination?.route

    val bottomNavRoutes = listOf(
        Router.Sounds,
        Router.Home,
        Router.CustomNoise
    )

    Scaffold(
        bottomBar = {
            if (bottomNavRoutes.any { it.route == currentRoute }) {
                NavBar(navController, bottomNavRoutes)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Router.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Router.Home.route) { HomeScreen(navController) }
            composable(Router.Sounds.route) { Library() }
            composable(Router.CustomNoise.route) { CustomNoise() }
        }
    }
}