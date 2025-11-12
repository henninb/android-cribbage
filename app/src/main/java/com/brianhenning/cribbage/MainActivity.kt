package com.brianhenning.cribbage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.model.GameSettings
import com.brianhenning.cribbage.ui.screens.CribbageMainScreen
import com.brianhenning.cribbage.ui.screens.SettingsScreen
import com.brianhenning.cribbage.ui.theme.CribbageTheme
import com.brianhenning.cribbage.ui.theme.ThemeCalculator

class MainActivity : ComponentActivity() {
    private lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesRepository = PreferencesRepository(applicationContext)

        setContent {
            // Manage theme and settings state at the root level
            var currentTheme by remember { mutableStateOf(ThemeCalculator.getCurrentTheme()) }
            var currentSettings by remember { mutableStateOf(GameSettings()) }
            var showSettings by remember { mutableStateOf(false) }

            // Load settings when activity starts
            LaunchedEffect(Unit) {
                currentSettings = preferencesRepository.loadGameSettings()
            }

            CribbageTheme(overrideTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(0.dp)  // No rounding on root surface
                ) {
                    if (showSettings) {
                        SettingsScreen(
                            currentSettings = currentSettings,
                            onSettingsChange = { newSettings ->
                                currentSettings = newSettings
                                preferencesRepository.saveGameSettings(newSettings)
                            },
                            onBackPressed = {
                                showSettings = false
                            }
                        )
                    } else {
                        CribbageMainScreen(
                            onThemeChange = { newTheme ->
                                currentTheme = newTheme
                            },
                            onSettingsClick = {
                                showSettings = true
                            }
                        )
                    }
                }
            }
        }
    }
}