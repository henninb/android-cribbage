package com.brianhenning.cribbage

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.brianhenning.cribbage.ui.composables.BottomNavBar
import com.brianhenning.cribbage.ui.navigation.Screen
import com.brianhenning.cribbage.ui.screens.FirstScreen
import com.brianhenning.cribbage.ui.screens.SecondScreen
import com.brianhenning.cribbage.ui.screens.ThirdScreen
import com.brianhenning.cribbage.ui.theme.CribbageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("CribbageGame", "MainActivity onCreate")
        setContent {
            Log.i("CribbageGame", "Setting up Compose content")
            CribbageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.i("CribbageGame", "MainActivity onResume")
    }
    
    override fun onPause() {
        super.onPause()
        Log.i("CribbageGame", "MainActivity onPause")
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Log.i("CribbageGame", "MainScreen composable function called")
    val items = listOf(
        Screen.First,
        Screen.Second,
        Screen.Third
    )
    
    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                items = items
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.First.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.First.route) {
                Log.i("CribbageGame", "Navigating to FirstScreen")
                FirstScreen()
            }
            composable(Screen.Second.route) {
                Log.i("CribbageGame", "Navigating to SecondScreen")
                SecondScreen(navController)
            }
            composable(Screen.Third.route) {
                Log.i("CribbageGame", "Navigating to ThirdScreen")
                ThirdScreen(navController)
            }
        }
    }
}