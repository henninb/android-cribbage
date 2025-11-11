package com.brianhenning.cribbage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.model.CardSelectionMode
import com.brianhenning.cribbage.model.CountingMode
import com.brianhenning.cribbage.model.GameSettings
import com.brianhenning.cribbage.ui.theme.CribbageTheme
import com.brianhenning.cribbage.ui.theme.LocalSeasonalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSettings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSeasonalTheme.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.colors.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(theme.colors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Selection Mode Section
            SettingSection(
                title = "Card Selection",
                description = "Choose how you want to select cards"
            )

            CardSelectionMode.entries.forEach { mode ->
                SettingOptionItem(
                    title = mode.displayName,
                    description = mode.description,
                    isSelected = currentSettings.cardSelectionMode == mode,
                    onClick = {
                        onSettingsChange(
                            currentSettings.copy(cardSelectionMode = mode)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Counting Mode Section
            SettingSection(
                title = "Counting Mode",
                description = "Choose how points are counted"
            )

            CountingMode.entries.forEach { mode ->
                SettingOptionItem(
                    title = mode.displayName,
                    description = mode.description,
                    isSelected = currentSettings.countingMode == mode,
                    onClick = {
                        onSettingsChange(
                            currentSettings.copy(countingMode = mode)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalSeasonalTheme.current

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SettingOptionItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSeasonalTheme.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                theme.colors.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Extension properties for display names and descriptions
private val CardSelectionMode.displayName: String
    get() = when (this) {
        CardSelectionMode.TAP -> "Tap"
        CardSelectionMode.LONG_PRESS -> "Long Press"
        CardSelectionMode.DRAG -> "Drag"
    }

private val CardSelectionMode.description: String
    get() = when (this) {
        CardSelectionMode.TAP -> "Single tap to select cards"
        CardSelectionMode.LONG_PRESS -> "Long press to select cards"
        CardSelectionMode.DRAG -> "Drag cards to discard area"
    }

private val CountingMode.displayName: String
    get() = when (this) {
        CountingMode.AUTOMATIC -> "Automatic"
        CountingMode.MANUAL -> "Manual"
    }

private val CountingMode.description: String
    get() = when (this) {
        CountingMode.AUTOMATIC -> "App calculates points automatically"
        CountingMode.MANUAL -> "Enter points manually"
    }

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CribbageTheme {
        SettingsScreen(
            currentSettings = GameSettings(),
            onSettingsChange = {},
            onBackPressed = {}
        )
    }
}
