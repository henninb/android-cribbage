package com.brianhenning.cribbage

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.brianhenning.cribbage.ui.screens.FirstScreen
import org.junit.Rule
import org.junit.Test

class FirstScreenComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    
    @Test
    fun firstScreen_displaysInitialState() {
        // Start the app with FirstScreen
        composeTestRule.setContent {
            FirstScreen()
        }
        
        // Initial state validations
        composeTestRule.onNodeWithText(context.getString(R.string.welcome_to_cribbage)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Game").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Score: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Opponent Score: 0").assertIsDisplayed()
    }
    
    @Test
    fun firstScreen_startGameButtonWorksCorrectly() {
        composeTestRule.setContent {
            FirstScreen()
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
            FirstScreen()
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
            FirstScreen()
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
            FirstScreen()
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