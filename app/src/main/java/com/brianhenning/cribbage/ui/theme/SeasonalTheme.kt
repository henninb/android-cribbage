package com.brianhenning.cribbage.ui.theme

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.Month
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

/**
 * Theme types for the cribbage game based on seasons and US secular holidays
 */
enum class ThemeType {
    // Seasons (Astronomical)
    SPRING,      // Mar 20 - Jun 20
    SUMMER,      // Jun 21 - Sep 21
    FALL,        // Sep 22 - Dec 20
    WINTER,      // Dec 21 - Mar 19

    // US Secular Holidays (take priority over seasons)
    NEW_YEAR,           // Jan 1
    MLK_DAY,            // 3rd Monday in January
    VALENTINES_DAY,     // Feb 14
    PRESIDENTS_DAY,     // 3rd Monday in February
    PI_DAY,             // Mar 14
    IDES_OF_MARCH,      // Mar 15
    ST_PATRICKS_DAY,    // Mar 17
    MEMORIAL_DAY,       // Last Monday in May
    INDEPENDENCE_DAY,   // Jul 4
    LABOR_DAY,          // 1st Monday in September
    HALLOWEEN,          // Oct 31
    THANKSGIVING,       // 4th Thursday in November
    CHRISTMAS           // Dec 25
}

/**
 * Color scheme for a specific theme
 */
data class ThemeColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val cardBack: Color,
    val boardPrimary: Color,
    val boardSecondary: Color,
    val accentLight: Color,
    val accentDark: Color
)

/**
 * Complete theme configuration including colors and decorative elements
 */
data class CribbageTheme(
    val type: ThemeType,
    val name: String,
    val colors: ThemeColors,
    val icon: String  // Unicode emoji or symbol
)

/**
 * Determines the current theme based on today's date
 */
object ThemeCalculator {

    fun getCurrentTheme(date: LocalDate = LocalDate.now()): CribbageTheme {
        // Check holidays first (they override seasons)
        getHolidayTheme(date)?.let { return it }

        // Fall back to seasonal theme
        return getSeasonalTheme(date)
    }

    private fun getHolidayTheme(date: LocalDate): CribbageTheme? {
        val month = date.month
        val dayOfMonth = date.dayOfMonth

        // New Year's Day (Jan 1) - extended to Jan 1-3
        if (month == Month.JANUARY && dayOfMonth in 1..3) {
            return ThemeDefinitions.NEW_YEAR
        }

        // MLK Day (3rd Monday in January)
        if (month == Month.JANUARY && isNthWeekdayOfMonth(date, DayOfWeek.MONDAY, 3)) {
            return ThemeDefinitions.MLK_DAY
        }

        // Valentine's Day (Feb 14) - extended to Feb 12-16
        if (month == Month.FEBRUARY && dayOfMonth in 12..16) {
            return ThemeDefinitions.VALENTINES_DAY
        }

        // Presidents' Day (3rd Monday in February)
        if (month == Month.FEBRUARY && isNthWeekdayOfMonth(date, DayOfWeek.MONDAY, 3)) {
            return ThemeDefinitions.PRESIDENTS_DAY
        }

        // Pi Day (Mar 14)
        if (month == Month.MARCH && dayOfMonth == 14) {
            return ThemeDefinitions.PI_DAY
        }

        // Ides of March (Mar 15)
        if (month == Month.MARCH && dayOfMonth == 15) {
            return ThemeDefinitions.IDES_OF_MARCH
        }

        // St. Patrick's Day (Mar 17) - extended to Mar 16-18
        if (month == Month.MARCH && dayOfMonth in 16..18) {
            return ThemeDefinitions.ST_PATRICKS_DAY
        }

        // Memorial Day (Last Monday in May)
        if (month == Month.MAY && isLastWeekdayOfMonth(date, DayOfWeek.MONDAY)) {
            return ThemeDefinitions.MEMORIAL_DAY
        }

        // Independence Day (Jul 4) - extended to Jul 2-6
        if (month == Month.JULY && dayOfMonth in 2..6) {
            return ThemeDefinitions.INDEPENDENCE_DAY
        }

        // Labor Day (1st Monday in September)
        if (month == Month.SEPTEMBER && isNthWeekdayOfMonth(date, DayOfWeek.MONDAY, 1)) {
            return ThemeDefinitions.LABOR_DAY
        }

        // Halloween (Oct 31) - extended to Oct 28-31
        if (month == Month.OCTOBER && dayOfMonth in 28..31) {
            return ThemeDefinitions.HALLOWEEN
        }

        // Thanksgiving (4th Thursday in November)
        if (month == Month.NOVEMBER && isNthWeekdayOfMonth(date, DayOfWeek.THURSDAY, 4)) {
            return ThemeDefinitions.THANKSGIVING
        }

        // Christmas (Dec 25) - extended to Dec 22-26
        if (month == Month.DECEMBER && dayOfMonth in 22..26) {
            return ThemeDefinitions.CHRISTMAS
        }

        return null
    }

    private fun getSeasonalTheme(date: LocalDate): CribbageTheme {
        val month = date.month
        val dayOfMonth = date.dayOfMonth

        return when {
            // Spring: March 20 - June 20
            (month == Month.MARCH && dayOfMonth >= 20) ||
            month == Month.APRIL ||
            month == Month.MAY ||
            (month == Month.JUNE && dayOfMonth <= 20) -> ThemeDefinitions.SPRING

            // Summer: June 21 - September 21
            (month == Month.JUNE && dayOfMonth >= 21) ||
            month == Month.JULY ||
            month == Month.AUGUST ||
            (month == Month.SEPTEMBER && dayOfMonth <= 21) -> ThemeDefinitions.SUMMER

            // Fall: September 22 - December 20
            (month == Month.SEPTEMBER && dayOfMonth >= 22) ||
            month == Month.OCTOBER ||
            month == Month.NOVEMBER ||
            (month == Month.DECEMBER && dayOfMonth <= 20) -> ThemeDefinitions.FALL

            // Winter: December 21 - March 19
            else -> ThemeDefinitions.WINTER
        }
    }

    private fun isNthWeekdayOfMonth(date: LocalDate, dayOfWeek: DayOfWeek, n: Int): Boolean {
        val firstOfMonth = date.withDayOfMonth(1)
        val nthOccurrence = firstOfMonth.with(TemporalAdjusters.dayOfWeekInMonth(n, dayOfWeek))
        return date == nthOccurrence
    }

    private fun isLastWeekdayOfMonth(date: LocalDate, dayOfWeek: DayOfWeek): Boolean {
        val lastOfMonth = date.with(TemporalAdjusters.lastDayOfMonth())
        val lastOccurrence = lastOfMonth.with(TemporalAdjusters.previousOrSame(dayOfWeek))
        return date == lastOccurrence
    }
}

/**
 * Predefined themes for all seasons and holidays
 */
object ThemeDefinitions {

    // ========== SEASONAL THEMES ==========

    val SPRING = CribbageTheme(
        type = ThemeType.SPRING,
        name = "Spring Renewal",
        colors = ThemeColors(
            primary = Color(0xFF388E3C),           // Fresh green (darker for better contrast)
            primaryVariant = Color(0xFF2E7D32),    // Dark green
            secondary = Color(0xFFF9A825),         // Yellow (sunshine)
            secondaryVariant = Color(0xFFF57F17),  // Golden
            background = Color(0xFFF9FBE7),        // Very light green background (lighter)
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFFAED581),          // Light green
            boardPrimary = Color(0xFF66BB6A),      // Medium green
            boardSecondary = Color(0xFF81C784),    // Light green
            accentLight = Color(0xFFFFCDD2),       // Pink (flowers)
            accentDark = Color(0xFF689F38)         // Lime green
        ),
        icon = "🌸"  // Cherry blossom
    )

    val SUMMER = CribbageTheme(
        type = ThemeType.SUMMER,
        name = "Summer Sun",
        colors = ThemeColors(
            primary = Color(0xFFF9A825),           // Amber (sun) - darker for contrast
            primaryVariant = Color(0xFFF57F17),    // Dark amber
            secondary = Color(0xFF0277BD),         // Sky blue - darker for contrast
            secondaryVariant = Color(0xFF01579B),  // Ocean blue
            background = Color(0xFFFFFDE7),        // Very light yellow (lighter)
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFFFFD54F),          // Yellow
            boardPrimary = Color(0xFFFFCA28),      // Bright yellow
            boardSecondary = Color(0xFF4FC3F7),    // Light blue
            accentLight = Color(0xFFB3E5FC),       // Pale blue
            accentDark = Color(0xFFE65100)         // Orange
        ),
        icon = "☀️"  // Sun
    )

    val FALL = CribbageTheme(
        type = ThemeType.FALL,
        name = "Autumn Harvest",
        colors = ThemeColors(
            primary = Color(0xFFFF8A50),           // Warm pumpkin orange - bright & readable
            primaryVariant = Color(0xFFE65100),    // Deep pumpkin
            secondary = Color(0xFFFFB74D),         // Harvest gold - bright & warm
            secondaryVariant = Color(0xFFFFA726),  // Golden amber
            background = Color(0xFF1A0E0A),        // Very dark brown (almost black) - maximum contrast
            surface = Color(0xFF2D1B16),           // Dark chocolate brown - good contrast
            cardBack = Color(0xFFFFCC80),          // Light golden peach - stands out
            boardPrimary = Color(0xFFD84315),      // Burnt orange/rust
            boardSecondary = Color(0xFFFFAB40),    // Bright golden
            accentLight = Color(0xFFFFF3E0),       // Cream/wheat - excellent for highlights
            accentDark = Color(0xFFBF360C)         // Deep rust red
        ),
        icon = "🍂"  // Fallen leaf
    )

    val WINTER = CribbageTheme(
        type = ThemeType.WINTER,
        name = "Winter Frost",
        colors = ThemeColors(
            primary = Color(0xFF1565C0),           // Blue - darker for contrast
            primaryVariant = Color(0xFF0D47A1),    // Dark blue
            secondary = Color(0xFF78909C),         // Blue grey - darker
            secondaryVariant = Color(0xFF546E7A),  // Dark blue grey
            background = Color(0xFF263238),        // Dark blue-grey background (darker)
            surface = Color(0xFF37474F),           // Dark surface
            cardBack = Color(0xFF90CAF9),          // Light blue
            boardPrimary = Color(0xFF42A5F5),      // Medium blue
            boardSecondary = Color(0xFF64B5F6),    // Sky blue
            accentLight = Color(0xFFFFFFFF),       // Snow white
            accentDark = Color(0xFF0D47A1)         // Navy blue
        ),
        icon = "❄️"  // Snowflake
    )

    // ========== HOLIDAY THEMES ==========

    val NEW_YEAR = CribbageTheme(
        type = ThemeType.NEW_YEAR,
        name = "New Year's Celebration",
        colors = ThemeColors(
            primary = Color(0xFFFFD700),           // Gold
            primaryVariant = Color(0xFFDAA520),    // Goldenrod
            secondary = Color(0xFF9C27B0),         // Purple
            secondaryVariant = Color(0xFF7B1FA2),  // Dark purple
            background = Color(0xFFFFF8E1),        // Light gold
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFFFFE082),          // Light gold
            boardPrimary = Color(0xFFFFD54F),      // Gold
            boardSecondary = Color(0xFFBA68C8),    // Purple
            accentLight = Color(0xFFFFFFFF),       // White (confetti)
            accentDark = Color(0xFF311B92)         // Deep purple
        ),
        icon = "🎉"  // Party popper
    )

    val MLK_DAY = CribbageTheme(
        type = ThemeType.MLK_DAY,
        name = "MLK Day - Equality",
        colors = ThemeColors(
            primary = Color(0xFF1976D2),           // Blue (equality)
            primaryVariant = Color(0xFF0D47A1),    // Navy blue
            secondary = Color(0xFF757575),         // Grey (unity)
            secondaryVariant = Color(0xFF424242),  // Dark grey
            background = Color(0xFFE3F2FD),        // Light blue
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFF90CAF9),          // Sky blue
            boardPrimary = Color(0xFF1E88E5),      // Blue
            boardSecondary = Color(0xFF9E9E9E),    // Grey
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF000000)         // Black
        ),
        icon = "✊"  // Raised fist
    )

    val VALENTINES_DAY = CribbageTheme(
        type = ThemeType.VALENTINES_DAY,
        name = "Valentine's Hearts",
        colors = ThemeColors(
            primary = Color(0xFFE91E63),           // Pink
            primaryVariant = Color(0xFFC2185B),    // Dark pink
            secondary = Color(0xFFF44336),         // Red
            secondaryVariant = Color(0xFFD32F2F),  // Dark red
            background = Color(0xFFFCE4EC),        // Light pink
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFFF8BBD0),          // Rose
            boardPrimary = Color(0xFFEC407A),      // Pink
            boardSecondary = Color(0xFFEF5350),    // Red
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF880E4F)         // Burgundy
        ),
        icon = "💕"  // Two hearts
    )

    val PRESIDENTS_DAY = CribbageTheme(
        type = ThemeType.PRESIDENTS_DAY,
        name = "Presidents' Day",
        colors = ThemeColors(
            primary = Color(0xFF1565C0),           // Presidential blue
            primaryVariant = Color(0xFF0D47A1),    // Navy
            secondary = Color(0xFFD32F2F),         // Red
            secondaryVariant = Color(0xFFB71C1C),  // Dark red
            background = Color(0xFFF5F5F5),        // Light grey
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFF90CAF9),          // Light blue
            boardPrimary = Color(0xFF1976D2),      // Blue
            boardSecondary = Color(0xFFE57373),    // Red
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF0D47A1)         // Navy
        ),
        icon = "🇺🇸"  // US Flag
    )

    val ST_PATRICKS_DAY = CribbageTheme(
        type = ThemeType.ST_PATRICKS_DAY,
        name = "St. Patrick's Green",
        colors = ThemeColors(
            primary = Color(0xFF43A047),           // Green
            primaryVariant = Color(0xFF2E7D32),    // Dark green
            secondary = Color(0xFFFFD700),         // Gold
            secondaryVariant = Color(0xFFDAA520),  // Goldenrod
            background = Color(0xFFC8E6C9),        // Light green
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFF81C784),          // Green
            boardPrimary = Color(0xFF66BB6A),      // Medium green
            boardSecondary = Color(0xFFFFE082),    // Gold
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF1B5E20)         // Deep green
        ),
        icon = "☘️"  // Shamrock
    )

    val MEMORIAL_DAY = CribbageTheme(
        type = ThemeType.MEMORIAL_DAY,
        name = "Memorial Day",
        colors = ThemeColors(
            primary = Color(0xFF1565C0),           // Blue
            primaryVariant = Color(0xFF0D47A1),    // Navy
            secondary = Color(0xFFD32F2F),         // Red
            secondaryVariant = Color(0xFFB71C1C),  // Dark red
            background = Color(0xFFECEFF1),        // Light grey
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFF90CAF9),          // Light blue
            boardPrimary = Color(0xFF1976D2),      // Blue
            boardSecondary = Color(0xFFE57373),    // Light red
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF37474F)         // Blue grey
        ),
        icon = "🎖️"  // Military medal
    )

    val INDEPENDENCE_DAY = CribbageTheme(
        type = ThemeType.INDEPENDENCE_DAY,
        name = "4th of July",
        colors = ThemeColors(
            primary = Color(0xFF1565C0),           // Blue
            primaryVariant = Color(0xFF0D47A1),    // Navy
            secondary = Color(0xFFD32F2F),         // Red
            secondaryVariant = Color(0xFFB71C1C),  // Dark red
            background = Color(0xFFF5F5F5),        // White
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFF90CAF9),          // Light blue
            boardPrimary = Color(0xFF1976D2),      // Blue
            boardSecondary = Color(0xFFE57373),    // Light red
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFFB71C1C)         // Dark red
        ),
        icon = "🎆"  // Fireworks
    )

    val LABOR_DAY = CribbageTheme(
        type = ThemeType.LABOR_DAY,
        name = "Labor Day",
        colors = ThemeColors(
            primary = Color(0xFF455A64),           // Blue grey (working)
            primaryVariant = Color(0xFF263238),    // Dark blue grey
            secondary = Color(0xFFFFB300),         // Amber (sunset)
            secondaryVariant = Color(0xFFF57C00),  // Orange
            background = Color(0xFFECEFF1),        // Light grey
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFF90A4AE),          // Grey blue
            boardPrimary = Color(0xFF546E7A),      // Blue grey
            boardSecondary = Color(0xFFFFCC80),    // Light orange
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF37474F)         // Dark blue grey
        ),
        icon = "⚒️"  // Hammer and pick
    )

    val HALLOWEEN = CribbageTheme(
        type = ThemeType.HALLOWEEN,
        name = "Halloween Spooky",
        colors = ThemeColors(
            primary = Color(0xFFFF6F00),           // Orange
            primaryVariant = Color(0xFFE65100),    // Dark orange
            secondary = Color(0xFF7E57C2),         // Purple
            secondaryVariant = Color(0xFF512DA8),  // Dark purple
            background = Color(0xFF212121),        // Dark (night)
            surface = Color(0xFF424242),           // Dark grey
            cardBack = Color(0xFFFFB74D),          // Light orange
            boardPrimary = Color(0xFFFF8F00),      // Pumpkin orange
            boardSecondary = Color(0xFF9575CD),    // Purple
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF000000)         // Black
        ),
        icon = "🎃"  // Jack-o-lantern
    )

    val THANKSGIVING = CribbageTheme(
        type = ThemeType.THANKSGIVING,
        name = "Thanksgiving Harvest",
        colors = ThemeColors(
            primary = Color(0xFFD84315),           // Burnt orange
            primaryVariant = Color(0xFFBF360C),    // Dark orange
            secondary = Color(0xFF8D6E63),         // Brown
            secondaryVariant = Color(0xFF5D4037),  // Dark brown
            background = Color(0xFFFBE9E7),        // Light orange
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFFFFAB91),          // Peach
            boardPrimary = Color(0xFFFF7043),      // Coral
            boardSecondary = Color(0xFFA1887F),    // Brown grey
            accentLight = Color(0xFFFFE0B2),       // Cream
            accentDark = Color(0xFF6D4C41)         // Deep brown
        ),
        icon = "🦃"  // Turkey
    )

    val CHRISTMAS = CribbageTheme(
        type = ThemeType.CHRISTMAS,
        name = "Christmas Cheer",
        colors = ThemeColors(
            primary = Color(0xFFC62828),           // Christmas red
            primaryVariant = Color(0xFFB71C1C),    // Dark red
            secondary = Color(0xFF2E7D32),         // Christmas green
            secondaryVariant = Color(0xFF1B5E20),  // Dark green
            background = Color(0xFFFAFAFA),        // Snow white
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFFEF9A9A),          // Light red
            boardPrimary = Color(0xFFE53935),      // Red
            boardSecondary = Color(0xFF43A047),    // Green
            accentLight = Color(0xFFFFFFFF),       // White (snow)
            accentDark = Color(0xFFFFD700)         // Gold
        ),
        icon = "🎄"  // Christmas tree
    )

    val PI_DAY = CribbageTheme(
        type = ThemeType.PI_DAY,
        name = "Pi Day 3.14159...",
        colors = ThemeColors(
            primary = Color(0xFF1976D2),           // Math blue
            primaryVariant = Color(0xFF0D47A1),    // Dark blue
            secondary = Color(0xFFFF6F00),         // Orange (circle)
            secondaryVariant = Color(0xFFE65100),  // Dark orange
            background = Color(0xFFE3F2FD),        // Light blue
            surface = Color(0xFFFFFFFF),           // White
            cardBack = Color(0xFF90CAF9),          // Light blue
            boardPrimary = Color(0xFF42A5F5),      // Blue
            boardSecondary = Color(0xFFFFB74D),    // Light orange
            accentLight = Color(0xFFFFFFFF),       // White
            accentDark = Color(0xFF01579B)         // Navy blue
        ),
        icon = "🥧"  // Pie
    )

    val IDES_OF_MARCH = CribbageTheme(
        type = ThemeType.IDES_OF_MARCH,
        name = "Ides of March - Beware!",
        colors = ThemeColors(
            primary = Color(0xFF8E24AA),           // Imperial purple
            primaryVariant = Color(0xFF6A1B9A),    // Dark purple
            secondary = Color(0xFFD32F2F),         // Roman red (blood)
            secondaryVariant = Color(0xFFB71C1C),  // Dark red
            background = Color(0xFFF3E5F5),        // Light purple
            surface = Color(0xFFFFFFFF),           // White (marble)
            cardBack = Color(0xFFCE93D8),          // Light purple
            boardPrimary = Color(0xFFAB47BC),      // Purple
            boardSecondary = Color(0xFFEF5350),    // Light red
            accentLight = Color(0xFFFFD700),       // Gold (Roman)
            accentDark = Color(0xFF4A148C)         // Deep purple
        ),
        icon = "🗡️"  // Dagger
    )
}
