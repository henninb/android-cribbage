package com.brianhenning.cribbage

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.brianhenning.cribbage.ui.screens.CribbageMainScreen
import com.brianhenning.cribbage.logic.PeggingRoundManager
import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.logic.SubRoundReset
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import org.junit.Rule
import org.junit.Test

class FirstScreenComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    
    @Test
    fun firstScreen_displaysInitialState() {
        // Start the app with CribbageMainScreen
        composeTestRule.setContent {
            CribbageMainScreen()
        }
        
        // Initial state validations
        composeTestRule.onNodeWithText(context.getString(R.string.welcome_to_cribbage)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Game").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Score: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Opponent Score: 0").assertIsDisplayed()
    }

    @Test
    fun peggingManager_goResetsAndAwardsGoPoint() {
        composeTestRule.setContent {
            val mgr = remember { PeggingRoundManager(Player.PLAYER) }
            var lastReset: SubRoundReset? by remember { mutableStateOf(null) }
            Column {
                Text("count: ${mgr.peggingCount}")
                Text("turn: ${mgr.isPlayerTurn}")
                Text("reset31: ${lastReset?.resetFor31 ?: false}")
                Text("goTo: ${lastReset?.goPointTo?.name ?: "none"}")

                Button(onClick = {
                    val outcome = mgr.onPlay(Card(Rank.TEN, Suit.CLUBS))
                    lastReset = outcome.reset
                }) { Text("PlayerPlay10") }

                Button(onClick = {
                    val outcome = mgr.onPlay(Card(Rank.JACK, Suit.HEARTS))
                    lastReset = outcome.reset
                }) { Text("OpponentPlayJ") }

                Button(onClick = {
                    lastReset = mgr.onGo(opponentHasLegalMove = false)
                }) { Text("GoNoMoves") }
            }
        }

        // Player plays 10 (turn -> Opponent), Opponent plays J (turn -> Player), Player says GO with no moves
        composeTestRule.onNodeWithText("PlayerPlay10").performClick()
        composeTestRule.onNodeWithText("OpponentPlayJ").performClick()
        composeTestRule.onNodeWithText("GoNoMoves").performClick()

        // After reset: count cleared, not a 31 reset, GO point to Opponent, next turn Player
        composeTestRule.onNodeWithText("count: 0").assertExists()
        composeTestRule.onNodeWithText("reset31: false").assertExists()
        composeTestRule.onNodeWithText("goTo: OPPONENT").assertExists()
        composeTestRule.onNodeWithText("turn: PLAYER").assertExists()
    }
    
    @Test
    fun firstScreen_startGameButtonWorksCorrectly() {
        composeTestRule.setContent {
            CribbageMainScreen()
        }
        
        // Click start game button
        composeTestRule.onNodeWithText("Start Game").performClick()
        
        // Verify state after starting game
        composeTestRule.onNodeWithText("End Game").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.deal_cards)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.game_started)).assertIsDisplayed()
    }
    
    @Test
    fun firstScreen_endGameResetsState() {
        composeTestRule.setContent {
            CribbageMainScreen()
        }
        
        // Start the game
        composeTestRule.onNodeWithText("Start Game").performClick()
        
        // End the game
        composeTestRule.onNodeWithText("End Game").performClick()
        
        // Verify state is reset
        composeTestRule.onNodeWithText(context.getString(R.string.welcome_to_cribbage)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Game").assertIsDisplayed()
    }
    
    @Test
    fun firstScreen_dealCardsCreatesPlayerHand() {
        composeTestRule.setContent {
            CribbageMainScreen()
        }
        
        // Start the game and deal cards
        composeTestRule.onNodeWithText("Start Game").performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.deal_cards)).performClick()
        
        // Verify cards are dealt
        composeTestRule.onNodeWithText(context.getString(R.string.select_cards_for_crib)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Select for Crib").assertIsDisplayed()
    }
    

    @Test
    fun firstScreen_selectingForCribWithoutTwoCardsShowsError() {
        composeTestRule.setContent {
            CribbageMainScreen()
        }
        
        // Start the game and deal cards
        composeTestRule.onNodeWithText("Start Game").performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.deal_cards)).performClick()
        
        // Try to select for crib without selecting any cards
        composeTestRule.onNodeWithText("Select for Crib").performClick()
        
        // Should show error
        composeTestRule.onNodeWithText(context.getString(R.string.select_exactly_two))
            .assertExists()
    }
}
