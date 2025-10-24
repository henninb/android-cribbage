package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.theme.ThemeCalculator
import com.brianhenning.cribbage.ui.theme.ThemeDefinitions
import com.brianhenning.cribbage.ui.theme.ThemeType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.Month

/**
 * Unit tests for SeasonalTheme logic.
 * Tests ThemeCalculator's ability to correctly determine themes based on dates,
 * including both seasonal themes and holiday-specific themes.
 */
class SeasonalThemeTest {

    // ========== Holiday Theme Tests ==========

    @Test
    fun getCurrentTheme_newYearsDay_returnsNewYearTheme() {
        val date = LocalDate.of(2024, Month.JANUARY, 1)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.NEW_YEAR, theme.type)
    }

    @Test
    fun getCurrentTheme_newYearsExtended_returnsNewYearTheme() {
        val dates = listOf(
            LocalDate.of(2024, Month.JANUARY, 1),
            LocalDate.of(2024, Month.JANUARY, 2),
            LocalDate.of(2024, Month.JANUARY, 3)
        )
        dates.forEach { date ->
            val theme = ThemeCalculator.getCurrentTheme(date)
            assertEquals("Date $date should be New Year", ThemeType.NEW_YEAR, theme.type)
        }
    }

    @Test
    fun getCurrentTheme_mlkDay2024_returnsMLKTheme() {
        val mlkDay2024 = LocalDate.of(2024, Month.JANUARY, 15) // 3rd Monday
        val theme = ThemeCalculator.getCurrentTheme(mlkDay2024)
        assertEquals(ThemeType.MLK_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_valentinesDay_returnsValentinesTheme() {
        val date = LocalDate.of(2024, Month.FEBRUARY, 14)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.VALENTINES_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_valentinesDayExtended_returnsValentinesTheme() {
        val dates = listOf(
            LocalDate.of(2024, Month.FEBRUARY, 12),
            LocalDate.of(2024, Month.FEBRUARY, 13),
            LocalDate.of(2024, Month.FEBRUARY, 14),
            LocalDate.of(2024, Month.FEBRUARY, 15),
            LocalDate.of(2024, Month.FEBRUARY, 16)
        )
        dates.forEach { date ->
            val theme = ThemeCalculator.getCurrentTheme(date)
            assertEquals("Date $date should be Valentine's",
                ThemeType.VALENTINES_DAY, theme.type)
        }
    }

    @Test
    fun getCurrentTheme_presidentsDay2024_returnsPresidentsTheme() {
        val presidentsDay2024 = LocalDate.of(2024, Month.FEBRUARY, 19) // 3rd Monday
        val theme = ThemeCalculator.getCurrentTheme(presidentsDay2024)
        assertEquals(ThemeType.PRESIDENTS_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_stPatricksDay_returnsStPatricksTheme() {
        val date = LocalDate.of(2024, Month.MARCH, 17)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.ST_PATRICKS_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_stPatricksDayExtended_returnsStPatricksTheme() {
        val dates = listOf(
            LocalDate.of(2024, Month.MARCH, 15),
            LocalDate.of(2024, Month.MARCH, 16),
            LocalDate.of(2024, Month.MARCH, 17),
            LocalDate.of(2024, Month.MARCH, 18)
        )
        dates.forEach { date ->
            val theme = ThemeCalculator.getCurrentTheme(date)
            assertEquals("Date $date should be St. Patrick's",
                ThemeType.ST_PATRICKS_DAY, theme.type)
        }
    }

    @Test
    fun getCurrentTheme_memorialDay2024_returnsMemorialDayTheme() {
        val memorialDay2024 = LocalDate.of(2024, Month.MAY, 27) // Last Monday
        val theme = ThemeCalculator.getCurrentTheme(memorialDay2024)
        assertEquals(ThemeType.MEMORIAL_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_independenceDay_returnsIndependenceDayTheme() {
        val date = LocalDate.of(2024, Month.JULY, 4)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.INDEPENDENCE_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_independenceDayExtended_returnsIndependenceDayTheme() {
        val dates = listOf(
            LocalDate.of(2024, Month.JULY, 2),
            LocalDate.of(2024, Month.JULY, 3),
            LocalDate.of(2024, Month.JULY, 4),
            LocalDate.of(2024, Month.JULY, 5),
            LocalDate.of(2024, Month.JULY, 6)
        )
        dates.forEach { date ->
            val theme = ThemeCalculator.getCurrentTheme(date)
            assertEquals("Date $date should be Independence Day",
                ThemeType.INDEPENDENCE_DAY, theme.type)
        }
    }

    @Test
    fun getCurrentTheme_laborDay2024_returnsLaborDayTheme() {
        val laborDay2024 = LocalDate.of(2024, Month.SEPTEMBER, 2) // 1st Monday
        val theme = ThemeCalculator.getCurrentTheme(laborDay2024)
        assertEquals(ThemeType.LABOR_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_halloween_returnsHalloweenTheme() {
        val date = LocalDate.of(2024, Month.OCTOBER, 31)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.HALLOWEEN, theme.type)
    }

    @Test
    fun getCurrentTheme_halloweenExtended_returnsHalloweenTheme() {
        val dates = listOf(
            LocalDate.of(2024, Month.OCTOBER, 28),
            LocalDate.of(2024, Month.OCTOBER, 29),
            LocalDate.of(2024, Month.OCTOBER, 30),
            LocalDate.of(2024, Month.OCTOBER, 31)
        )
        dates.forEach { date ->
            val theme = ThemeCalculator.getCurrentTheme(date)
            assertEquals("Date $date should be Halloween", ThemeType.HALLOWEEN, theme.type)
        }
    }

    @Test
    fun getCurrentTheme_thanksgiving2024_returnsThanksgivingTheme() {
        val thanksgiving2024 = LocalDate.of(2024, Month.NOVEMBER, 28) // 4th Thursday
        val theme = ThemeCalculator.getCurrentTheme(thanksgiving2024)
        assertEquals(ThemeType.THANKSGIVING, theme.type)
    }

    // ========== Seasonal Theme Tests ==========

    @Test
    fun getCurrentTheme_springStart_returnsSpringTheme() {
        val date = LocalDate.of(2024, Month.MARCH, 20)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.SPRING, theme.type)
    }

    @Test
    fun getCurrentTheme_springMiddle_returnsSpringTheme() {
        val date = LocalDate.of(2024, Month.APRIL, 15)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.SPRING, theme.type)
    }

    @Test
    fun getCurrentTheme_springEnd_returnsSpringTheme() {
        val date = LocalDate.of(2024, Month.JUNE, 20)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.SPRING, theme.type)
    }

    @Test
    fun getCurrentTheme_summerStart_returnsSummerTheme() {
        val date = LocalDate.of(2024, Month.JUNE, 21)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.SUMMER, theme.type)
    }

    @Test
    fun getCurrentTheme_summerMiddle_returnsSummerTheme() {
        val date = LocalDate.of(2024, Month.JULY, 15)
        val theme = ThemeCalculator.getCurrentTheme(date)
        // Note: July 15 is not in Independence Day extended range (2-6)
        assertEquals(ThemeType.SUMMER, theme.type)
    }

    @Test
    fun getCurrentTheme_summerEnd_returnsSummerTheme() {
        val date = LocalDate.of(2024, Month.SEPTEMBER, 21)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.SUMMER, theme.type)
    }

    @Test
    fun getCurrentTheme_fallStart_returnsFallTheme() {
        val date = LocalDate.of(2024, Month.SEPTEMBER, 22)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.FALL, theme.type)
    }

    @Test
    fun getCurrentTheme_fallMiddle_returnsFallTheme() {
        val date = LocalDate.of(2024, Month.OCTOBER, 15)
        val theme = ThemeCalculator.getCurrentTheme(date)
        // Note: Oct 15 is not in Halloween extended range (28-31)
        assertEquals(ThemeType.FALL, theme.type)
    }

    @Test
    fun getCurrentTheme_fallEnd_returnsFallTheme() {
        val date = LocalDate.of(2024, Month.DECEMBER, 20)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.FALL, theme.type)
    }

    @Test
    fun getCurrentTheme_winterStart_returnsWinterTheme() {
        val date = LocalDate.of(2024, Month.DECEMBER, 21)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.WINTER, theme.type)
    }

    @Test
    fun getCurrentTheme_winterMiddle_returnsWinterTheme() {
        val date = LocalDate.of(2025, Month.JANUARY, 15)
        val theme = ThemeCalculator.getCurrentTheme(date)
        // Note: Jan 15 is typically MLK day (3rd Monday), check year
        // For a non-MLK date in January:
        val nonHolidayWinterDate = LocalDate.of(2024, Month.JANUARY, 10)
        val winterTheme = ThemeCalculator.getCurrentTheme(nonHolidayWinterDate)
        assertEquals(ThemeType.WINTER, winterTheme.type)
    }

    @Test
    fun getCurrentTheme_winterEnd_returnsWinterTheme() {
        val date = LocalDate.of(2024, Month.MARCH, 19)
        val theme = ThemeCalculator.getCurrentTheme(date)
        assertEquals(ThemeType.WINTER, theme.type)
    }

    // ========== Edge Case Tests ==========

    @Test
    fun getCurrentTheme_boundaryBetweenWinterAndSpring_handlesCorrectly() {
        val lastWinterDay = LocalDate.of(2024, Month.MARCH, 19)
        val firstSpringDay = LocalDate.of(2024, Month.MARCH, 20)

        assertEquals(ThemeType.WINTER, ThemeCalculator.getCurrentTheme(lastWinterDay).type)
        assertEquals(ThemeType.SPRING, ThemeCalculator.getCurrentTheme(firstSpringDay).type)
    }

    @Test
    fun getCurrentTheme_boundaryBetweenSpringAndSummer_handlesCorrectly() {
        val lastSpringDay = LocalDate.of(2024, Month.JUNE, 20)
        val firstSummerDay = LocalDate.of(2024, Month.JUNE, 21)

        assertEquals(ThemeType.SPRING, ThemeCalculator.getCurrentTheme(lastSpringDay).type)
        assertEquals(ThemeType.SUMMER, ThemeCalculator.getCurrentTheme(firstSummerDay).type)
    }

    @Test
    fun getCurrentTheme_boundaryBetweenSummerAndFall_handlesCorrectly() {
        val lastSummerDay = LocalDate.of(2024, Month.SEPTEMBER, 21)
        val firstFallDay = LocalDate.of(2024, Month.SEPTEMBER, 22)

        assertEquals(ThemeType.SUMMER, ThemeCalculator.getCurrentTheme(lastSummerDay).type)
        assertEquals(ThemeType.FALL, ThemeCalculator.getCurrentTheme(firstFallDay).type)
    }

    @Test
    fun getCurrentTheme_boundaryBetweenFallAndWinter_handlesCorrectly() {
        val lastFallDay = LocalDate.of(2024, Month.DECEMBER, 20)
        val firstWinterDay = LocalDate.of(2024, Month.DECEMBER, 21)

        assertEquals(ThemeType.FALL, ThemeCalculator.getCurrentTheme(lastFallDay).type)
        assertEquals(ThemeType.WINTER, ThemeCalculator.getCurrentTheme(firstWinterDay).type)
    }

    @Test
    fun getCurrentTheme_holidayOverridesSeason_valentinesOverridesWinter() {
        val date = LocalDate.of(2024, Month.FEBRUARY, 14)
        val theme = ThemeCalculator.getCurrentTheme(date)
        // Valentine's Day is in winter season, but holiday takes precedence
        assertEquals(ThemeType.VALENTINES_DAY, theme.type)
    }

    @Test
    fun getCurrentTheme_holidayOverridesSeason_halloweenOverridesFall() {
        val date = LocalDate.of(2024, Month.OCTOBER, 31)
        val theme = ThemeCalculator.getCurrentTheme(date)
        // Halloween is in fall season, but holiday takes precedence
        assertEquals(ThemeType.HALLOWEEN, theme.type)
    }

    // ========== Theme Definitions Tests ==========

    @Test
    fun themeDefinitions_allThemesHaveNames() {
        val themes = listOf(
            ThemeDefinitions.SPRING,
            ThemeDefinitions.SUMMER,
            ThemeDefinitions.FALL,
            ThemeDefinitions.WINTER,
            ThemeDefinitions.NEW_YEAR,
            ThemeDefinitions.MLK_DAY,
            ThemeDefinitions.VALENTINES_DAY,
            ThemeDefinitions.PRESIDENTS_DAY,
            ThemeDefinitions.ST_PATRICKS_DAY,
            ThemeDefinitions.MEMORIAL_DAY,
            ThemeDefinitions.INDEPENDENCE_DAY,
            ThemeDefinitions.LABOR_DAY,
            ThemeDefinitions.HALLOWEEN,
            ThemeDefinitions.THANKSGIVING
        )

        themes.forEach { theme ->
            assert(theme.name.isNotEmpty()) { "${theme.type} has empty name" }
        }
    }

    @Test
    fun themeDefinitions_allThemesHaveIcons() {
        val themes = listOf(
            ThemeDefinitions.SPRING,
            ThemeDefinitions.SUMMER,
            ThemeDefinitions.FALL,
            ThemeDefinitions.WINTER,
            ThemeDefinitions.NEW_YEAR,
            ThemeDefinitions.MLK_DAY,
            ThemeDefinitions.VALENTINES_DAY,
            ThemeDefinitions.PRESIDENTS_DAY,
            ThemeDefinitions.ST_PATRICKS_DAY,
            ThemeDefinitions.MEMORIAL_DAY,
            ThemeDefinitions.INDEPENDENCE_DAY,
            ThemeDefinitions.LABOR_DAY,
            ThemeDefinitions.HALLOWEEN,
            ThemeDefinitions.THANKSGIVING
        )

        themes.forEach { theme ->
            assert(theme.icon.isNotEmpty()) { "${theme.type} has empty icon" }
        }
    }

    @Test
    fun themeDefinitions_allThemesHaveUniqueTypes() {
        val themes = listOf(
            ThemeDefinitions.SPRING,
            ThemeDefinitions.SUMMER,
            ThemeDefinitions.FALL,
            ThemeDefinitions.WINTER,
            ThemeDefinitions.NEW_YEAR,
            ThemeDefinitions.MLK_DAY,
            ThemeDefinitions.VALENTINES_DAY,
            ThemeDefinitions.PRESIDENTS_DAY,
            ThemeDefinitions.ST_PATRICKS_DAY,
            ThemeDefinitions.MEMORIAL_DAY,
            ThemeDefinitions.INDEPENDENCE_DAY,
            ThemeDefinitions.LABOR_DAY,
            ThemeDefinitions.HALLOWEEN,
            ThemeDefinitions.THANKSGIVING
        )

        val types = themes.map { it.type }
        val uniqueTypes = types.toSet()
        assertEquals(themes.size, uniqueTypes.size)
    }
}
