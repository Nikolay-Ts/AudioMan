package com.sonnenstahl.audioman

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sonnenstahl.audioman.ui.theme.AudioManTheme
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.Router
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.filter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.distinctUntilChanged
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioManTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val currentBackStack = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack.value?.destination?.route
    LaunchedEffect(Unit){

        Log.d("IN MAIN MAU", "${AudioPlayer.isActive}")
        AudioPlayer.isActive
            .filter { it == true }
            .collect {
                AudioPlayer.countDown()
            }
    }

    val bottomNavRoutes =
        listOf(
            Router.Sounds,
            Router.Home,
            Router.CustomNoise,
        )

    Scaffold(
        bottomBar = {
            if (bottomNavRoutes.any { it.route == currentRoute }) {
                NavBar(navController, bottomNavRoutes)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Router.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Router.Home.route) { HomeScreen(navController) }
            composable(Router.Sounds.route) { Library() }
            composable(Router.CustomNoise.route) { CustomNoise() }
        }
    }
}
