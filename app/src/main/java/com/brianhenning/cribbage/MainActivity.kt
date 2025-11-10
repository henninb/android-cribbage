package com.brianhenning.cribbage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.ui.screens.CribbageMainScreen
import com.brianhenning.cribbage.ui.theme.CribbageTheme
import com.brianhenning.cribbage.ui.theme.ThemeCalculator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Manage theme state at the root level
            var currentTheme by remember { mutableStateOf(ThemeCalculator.getCurrentTheme()) }

            CribbageTheme(overrideTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(0.dp)  // No rounding on root surface
                ) {
                    CribbageMainScreen(
                        onThemeChange = { newTheme ->
                            currentTheme = newTheme
                        }
                    )
                }
            }
        }
    }
}