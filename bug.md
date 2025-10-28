> you need to ensure that the transition from the pegging part of the game to the counting part of the game always happens after the pegging portion is completed. right now that is not happening for some or
all cases. fix this.

● Good catch! The issue is that when we set pendingReset, we're pausing the game before checking if pegging is complete. The check for whether pegging is finished happens inside applyManagerReset, which
  won't be called until the user clicks "Next Round". Let me trace through the logic and fix this.


● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 26 additions
       260            awardPeggingPoints(isPlayer, pts, cardPlayed)
       261        }
       262
       263 +      // Check if pegging phase is complete and transition to hand counting
       264 +      fun checkPeggingComplete() {
       265 +          // Check if all 8 cards have been played
       266 +          if (playerCardsPlayed.size == 4 && opponentCardsPlayed.size == 4) {
       267 +              isPeggingPhase = false
       268 +              currentPhase = GamePhase.HAND_COUNTING
       269 +              gameStatus += "\nPegging phase complete. Proceed to hand scoring."
       270 +              showHandCountingButton = true
       271 +              return
       272 +          }
       273 +
       274 +          // Check if no legal moves remain for either player
       275 +          val playerLegal = playerHand.filterIndexed { index, card ->
       276 +              !playerCardsPlayed.contains(index) && (card.getValue() + peggingCount <= 31)
       277 +          }
       278 +          val opponentLegal = opponentHand.filterIndexed { index, card ->
       279 +              !opponentCardsPlayed.contains(index) && (card.getValue() + peggingCount <= 31)
       280 +          }
       281 +          if (playerLegal.isEmpty() && opponentLegal.isEmpty()) {
       282 +              isPeggingPhase = false
       283 +              currentPhase = GamePhase.HAND_COUNTING
       284 +              gameStatus += "\nPegging phase complete. Proceed to hand scoring."
       285 +              showHandCountingButton = true
       286 +          }
       287 +      }
       288 +



● Now let me update handleNextRound to check for completion after applying the reset:

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 2 additions
       627            pendingReset?.let { pending ->
       628                applyManagerReset(pending.resetData)
       629                pendingReset = null
       630 +              // Check if pegging is now complete after the reset
       631 +              checkPeggingComplete()
       632            }
       633        }
       634

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 9 additions and 4 removals
       977                                    }
       978
       979                                    if (oppOutcome.reset == null) {
       980 -                                      val playerPlayable = playerHand.filterIndexed { index, card ->
       981 -                                          !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       982 -                                      }
       983 -                                      if (playerPlayable.isEmpty()) {
       980 +                                      // Check if pegging is complete after opponent's play
       981 +                                      checkPeggingComplete()
       982 +
       983 +                                      // Only continue if still in pegging phase
       984 +                                      if (isPeggingPhase) {
       985 +                                          val playerPlayable = playerHand.filterIndexed { index, card ->
       986 +                                              !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       987 +                                          }
       988 +                                          if (playerPlayable.isEmpty()) {
       989                                            // Only show Go button if player has cards left; otherwise auto-handle
       990                                            if (playerCardsPlayed.size < 4) {
       991                                                showGoButton = true

● Now I need to close the if (isPeggingPhase) block:

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 1 addition
       1002                                            showGoButton = false
       1003                                            gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
       1004                                        }
       1005 +                                      }
       1006                                    }
       1007                                } else {
       1008                                    // Opponent has no legal card to play

● Now let me find and update the other opponent play locations. Let me check the opponent's play after Go:

● Read(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Read 50 lines

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 9 additions and 4 removals
       564                            }
       565
       566                            if (oppOutcome.reset == null) {
       567 -                              val playerPlayableAfter = playerHand.filterIndexed { index, card ->
       568 -                                  !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       569 -                              }
       570 -                              if (playerPlayableAfter.isEmpty()) {
       567 +                              // Check if pegging is complete after opponent's play
       568 +                              checkPeggingComplete()
       569 +
       570 +                              // Only continue if still in pegging phase
       571 +                              if (isPeggingPhase) {
       572 +                                  val playerPlayableAfter = playerHand.filterIndexed { index, card ->
       573 +                                      !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       574 +                                  }
       575 +                                  if (playerPlayableAfter.isEmpty()) {
       576                                    // Only show Go button if player has cards left; otherwise auto-handle
       577                                    if (playerCardsPlayed.size < 4) {
       578                                        showGoButton = true

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 1 addition
       589                                    showGoButton = false
       590                                    gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
       591                                }
       592 +                              }
       593                            }
       594                        } else {
       595                            autoHandleGoRef.value()

● Let me find and update opponent's first play location:

● Read(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Read 50 lines

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 9 additions and 4 removals
       867                                }
       868
       869                                if (outcome.reset == null) {
       870 -                                  val playerPlayable = playerHand.filterIndexed { index, card ->
       871 -                                      !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       872 -                                  }
       873 -                                  if (playerPlayable.isEmpty()) {
       870 +                                  // Check if pegging is complete after opponent's play
       871 +                                  checkPeggingComplete()
       872 +
       873 +                                  // Only continue if still in pegging phase
       874 +                                  if (isPeggingPhase) {
       875 +                                      val playerPlayable = playerHand.filterIndexed { index, card ->
       876 +                                          !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       877 +                                      }
       878 +                                      if (playerPlayable.isEmpty()) {
       879                                        // Only show Go button if player has cards left; otherwise auto-handle
       880                                        if (playerCardsPlayed.size < 4) {
       881                                            showGoButton = true

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 1 addition
       891                                        playCardButtonEnabled = true
       892                                        showGoButton = false
       893                                    }
       894 +                                  }
       895                                }
       896                            } else {
       897                                autoHandleGoRef.value()

● Let me find and update the opponent play location after applyManagerReset:

● Read(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Read 50 lines

● Update(app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt)
  ⎿  Updated app/src/main/java/com/brianhenning/cribbage/ui/screens/CribbageMainScreen.kt with 9 additions and 4 removals
       417                        }
       418
       419                        if (outcome.reset == null) {
       420 -                          val playerPlayable = playerHand.filterIndexed { index, card ->
       421 -                              !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       422 -                          }
       423 -                          if (playerPlayable.isEmpty()) {
       420 +                          // Check if pegging is complete after opponent's play
       421 +                          checkPeggingComplete()
       422 +
       423 +                          // Only continue if still in pegging phase
       424 +                          if (isPeggingPhase) {
       425 +                              val playerPlayable = playerHand.filterIndexed { index, card ->
       426 +                                  !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
       427 +                              }
       428 +                              if (playerPlayable.isEmpty()) {
       429                                // Only show Go button if player has cards left; otherwise auto-handle
       430                                if (playerCardsPlayed.size < 4) {
       431                                    showGoButton = true
  ⎿  Session limit reached ∙ resets 10am
     /upgrade to increase your usage limit.
