package com.brianhenning.cribbage.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.game.state.GameLifecycleManager
import com.brianhenning.cribbage.game.state.HandCountingStateManager
import com.brianhenning.cribbage.game.state.PeggingStateManager
import com.brianhenning.cribbage.game.state.ScoreManager

/**
 * Factory for creating CribbageGameViewModel with all dependencies.
 *
 * Usage:
 * ```kotlin
 * val viewModel: CribbageGameViewModel = viewModel(
 *     factory = CribbageGameViewModelFactory(context)
 * )
 * ```
 */
class CribbageGameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CribbageGameViewModel::class.java)) {
            // Create dependencies
            val preferencesRepository = PreferencesRepository(context)
            val scoreManager = ScoreManager(preferencesRepository)
            val lifecycleManager = GameLifecycleManager(context, preferencesRepository)
            val peggingManager = PeggingStateManager()
            val handCountingManager = HandCountingStateManager(scoreManager)

            // Create ViewModel
            return CribbageGameViewModel(
                context = context,
                lifecycleManager = lifecycleManager,
                peggingManager = peggingManager,
                handCountingManager = handCountingManager,
                scoreManager = scoreManager,
                preferencesRepository = preferencesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
