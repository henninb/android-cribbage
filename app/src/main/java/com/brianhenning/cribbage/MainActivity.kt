package com.brianhenning.cribbage

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brianhenning.cribbage.ui.screens.FirstScreen
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
    Log.i("CribbageGame", "MainScreen composable function called")
    FirstScreen()
}