# Pegging Test Plan - Android Cribbage App

**Date:** October 24, 2025
**Current Status:** ✅ All 129 tests passing
**Test Files:** 10 pegging-specific test suites

---

## Current Test Coverage

### Test Suites Summary

| Test Suite | Tests | Status | Coverage |
|------------|-------|--------|----------|
| `Pegging31ScoringTest` | 7 | ✅ PASS | 31 scenarios, multiple scoring |
| `PeggingRoundManagerComplexScenariosTest` | 21 | ✅ PASS | Multi-turn sequences, resets |
| `PeggingRoundManagerEdgeCasesTest` | 25 | ✅ PASS | Boundary conditions, edge cases |
| `PeggingRoundManagerMultipleGoTest` | 11 | ✅ PASS | Consecutive GOs |
| `PeggingRoundManagerTest` | 5 | ✅ PASS | Basic GO and turn management |
| `PeggingScorerCombosTest` | 4 | ✅ PASS | Scoring combinations |
| `PeggingScorerComplexScenariosTest` | 32 | ✅ PASS | Complex scoring scenarios |
| `PeggingScorerEdgeCasesTest` | 2 | ✅ PASS | Scoring edge cases |
| `PeggingScorerLongerRunsTest` | 15 | ✅ PASS | Runs of 6-7 cards |
| `PeggingScorerTest` | 7 | ✅ PASS | Basic scoring logic |
| **TOTAL** | **129** | **✅** | **Comprehensive** |

---

## Test Execution Plan

### Continuous Testing
```bash
# Run all tests
./gradlew test

# Run with fresh build
./gradlew clean test

# Run with detailed output
./gradlew test --info

# Re-run tests (bypass UP-TO-DATE)
./gradlew test --rerun-tasks
```

### Test Results Location
```
app/build/reports/tests/testDebugUnitTest/index.html
app/build/test-results/testDebugUnitTest/*.xml
```

---

## Areas Tested (✅ = Covered, ⚠️ = Partial, ❌ = Missing)

### GO Logic
- ✅ GO declaration with opponent can play (turn transfer)
- ✅ GO declaration with opponent cannot play (reset)
- ✅ Consecutive GOs from both players
- ✅ GO point awarded to last player who played
- ✅ GO with no cards played (edge case)
- ✅ Multiple GO cycles in one round
- ✅ GO followed by play followed by GO
- ✅ Turn switching after GO reset

### 31 Logic
- ✅ Reaching exactly 31 triggers immediate reset
- ✅ No GO point awarded for 31 (2 points for 31 only)
- ✅ 31 with additional scoring (pairs, runs, fifteens)
- ✅ Turn switching after 31 (opposite player goes first)
- ✅ Multiple paths to 31
- ✅ 31 after opponent said GO

### State Management
- ✅ Initial state (Player vs Opponent starting)
- ✅ Count tracking and incrementing
- ✅ Pile maintenance (adding, clearing)
- ✅ lastPlayerWhoPlayed tracking
- ✅ consecutiveGoes counter
- ✅ Turn switching after each play
- ✅ State clearing after reset
- ✅ Input validation (card doesn't exceed 31)

### Scoring Logic
- ✅ Fifteens (2 points)
- ✅ Thirty-ones (2 points)
- ✅ Pairs (2/6/12 points)
- ✅ Runs (3-7 cards)
- ✅ Runs out of order (5-4-6 is valid)
- ✅ Duplicates break runs
- ✅ Multiple scoring combinations
- ✅ No scoring scenarios

### Edge Cases
- ✅ Empty pile
- ✅ Single card pile
- ✅ No cards played, both GO
- ✅ Face card values (J/Q/K = 10)
- ✅ Ace value (A = 1)
- ✅ Maximum run length (7 cards)
- ✅ No wrapping runs (K-A-2 invalid)
- ✅ Multiple reset cycles
- ✅ Complex multi-turn scenarios

---

## Gap Analysis

### Potentially Missing Tests

#### 1. Integration Tests (Lower Priority)
⚠️ **Full Round Simulation**
- Current: Individual sub-rounds tested
- Gap: No tests simulating complete 8-card pegging round
- Impact: Low (unit tests are comprehensive)
- Recommendation: Consider adding integration tests

#### 2. Performance Tests (Lower Priority)
⚠️ **Large Pile Performance**
- Current: Tests up to 7-card runs
- Gap: No performance benchmarks
- Impact: Very Low (typical piles are small)
- Recommendation: Optional

#### 3. Concurrency Tests (Not Applicable)
❌ **Thread Safety**
- Current: Not tested
- Gap: If app uses threading for pegging logic
- Impact: Unknown (depends on architecture)
- Recommendation: Verify if PeggingRoundManager is used across threads

### Covered Edge Cases ✅

All critical edge cases are tested:
- ✅ Boundary values (0, 15, 30, 31)
- ✅ Null handling (no lastPlayerWhoPlayed)
- ✅ Empty states (no cards played)
- ✅ Maximum values (4 of a kind, 7-card runs)
- ✅ Turn alternation in complex scenarios
- ✅ Reset cycles

---

## Recommendations for Test Maintenance

### High Priority (Maintain Current)
1. ✅ **Keep all 129 tests passing** - Run before every commit
2. ✅ **Add tests for new features** - Follow TDD approach
3. ✅ **Review tests after rule clarifications** - Update if rules change

### Medium Priority (Enhancement)
4. ⚠️ **Add integration tests** - Simulate full pegging rounds
5. ⚠️ **Property-based testing** - Generate random valid game sequences
6. ⚠️ **Mutation testing** - Verify tests catch intentional bugs

### Low Priority (Optional)
7. ⚠️ **Performance benchmarks** - Track algorithm efficiency
8. ⚠️ **Test coverage metrics** - Measure line/branch coverage
9. ⚠️ **Regression test suite** - Mark critical tests for smoke testing

---

## Test Strategy for Future Changes

### Before Making Changes
1. Run full test suite: `./gradlew test`
2. Verify all 129 pegging tests pass
3. Review failing tests if any

### After Making Changes
1. Add tests for new behavior FIRST (TDD)
2. Run tests continuously during development
3. Verify no regressions (all previous tests still pass)
4. Add edge case tests for new scenarios
5. Update documentation if rules change

### Example: Adding New GO Scenario
```kotlin
@Test
fun newScenario_description() {
    val manager = PeggingRoundManager(Player.PLAYER)

    // Arrange: Set up scenario
    // Act: Perform actions
    // Assert: Verify expected behavior

    assertEquals(expected, actual)
}
```

---

## Regression Test Plan

### Critical Tests to Always Run

#### GO Point Award (5 critical tests)
1. `PeggingRoundManagerTest::bothSayGo_resets_andAwardsToLastPlayer`
2. `PeggingRoundManagerMultipleGoTest::bothPlayersGO_causesReset_withGoPoint`
3. `PeggingRoundManagerEdgeCasesTest::onGo_afterNoPlays_noGoPointAwarded`
4. `PeggingRoundManagerMultipleGoTest::goPoint_awardedToLastPlayerWhoPlayed`
5. `PeggingRoundManagerComplexScenariosTest::pegging_GOReset_awardsToLastPlayer`

#### 31 Reset (4 critical tests)
1. `PeggingRoundManagerEdgeCasesTest::onPlay_reachingExactly31_triggersReset`
2. `Pegging31ScoringTest::testHitting31Exactly_shouldScore2Points`
3. `PeggingRoundManagerMultipleGoTest::play31_resetsImmediately_noGoPoint`
4. `PeggingRoundManagerComplexScenariosTest::pegging_31Reset_noGOPointAwarded`

#### Turn Switching (3 critical tests)
1. `PeggingRoundManagerEdgeCasesTest::reset_switchesToOtherPlayer`
2. `PeggingRoundManagerMultipleGoTest::resetAfter31_nextPlayerIsCorrect`
3. `PeggingRoundManagerComplexScenariosTest::pegging_afterReset_nextPlayerIsOppositeOfLast`

### Smoke Test Command
```bash
# Run critical tests (if filtering supported)
./gradlew test --tests "*bothSayGo*"
./gradlew test --tests "*31*"
```

---

## Coverage Gaps Assessment

### Current Coverage: ✅ EXCELLENT

**Strengths:**
- Comprehensive GO scenarios
- All 31 edge cases covered
- Turn management thoroughly tested
- Edge cases well represented
- Complex multi-turn scenarios tested

**No Critical Gaps Found**

### Optional Enhancements

#### 1. Integration Test Example
```kotlin
@Test
fun fullPeggingRound_eightCards_allScenarios() {
    // Simulate complete 8-card pegging round
    // Including multiple sub-rounds, GOs, and 31s
}
```

#### 2. Property-Based Test Example
```kotlin
@Test
fun randomGameSequence_alwaysValidState() {
    // Generate 1000 random valid game sequences
    // Verify state invariants hold
}
```

---

## Action Items

### Immediate (Next Commit)
- ✅ NO CHANGES NEEDED - All tests passing
- ✅ Keep running tests before commits
- ✅ Review this plan before adding new features

### Short-term (Next Sprint)
- ⚠️ Consider adding integration tests (optional)
- ⚠️ Document cribbage rules in code comments
- ⚠️ Set up CI/CD to run tests automatically

### Long-term (Future)
- ⚠️ Add property-based testing framework
- ⚠️ Performance profiling for pegging logic
- ⚠️ Mutation testing to verify test quality

---

## Test Quality Metrics

### Current Status
- **Test Count:** 129 pegging tests
- **Pass Rate:** 100% (129/129)
- **Failure Rate:** 0% (0/129)
- **Coverage:** Comprehensive (all GO scenarios covered)
- **Maintainability:** ✅ Excellent (clear, organized tests)
- **Readability:** ✅ Excellent (descriptive names and assertions)

### Quality Indicators
✅ Tests are independent (can run in any order)
✅ Tests have clear arrange-act-assert structure
✅ Tests use descriptive names
✅ Tests include helpful assertion messages
✅ Tests cover edge cases
✅ Tests are organized by scenario type

---

## Conclusion

**Current State:** ✅ **EXCELLENT**

The pegging test suite is comprehensive, well-organized, and thoroughly covers all GO scenarios, 31 resets, turn management, and edge cases. No bugs were found in the GO logic.

**Recommendation:** **MAINTAIN CURRENT QUALITY**

No immediate changes needed. Continue running tests before commits and follow TDD for new features.

---

## Quick Reference Commands

```bash
# Run all tests
./gradlew test

# Force re-run
./gradlew test --rerun-tasks

# Clean and test
./gradlew clean test

# View results
open app/build/reports/tests/testDebugUnitTest/index.html

# Check for test failures
grep -r "failures=\"0\"" app/build/test-results/testDebugUnitTest/
```

---

**Document Status:** ✅ Complete
**Next Review:** After any pegging logic changes
**Owner:** Development Team
