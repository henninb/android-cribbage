package com.brianhenning.cribbage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Comprehensive tests for data classes used in network requests and responses.
 * Tests equality, serialization readiness, and proper handling of nullable fields.
 */
class DataClassValidationTest {

    // ========== LoginRequest Tests ==========

    @Test
    fun loginRequest_canBeCreated_withValidCredentials() {
        val request = LoginRequest("test@example.com", "password123")

        assertEquals("test@example.com", request.email)
        assertEquals("password123", request.password)
    }

    @Test
    fun loginRequest_handlesEmptyStrings() {
        val request = LoginRequest("", "")

        assertEquals("", request.email)
        assertEquals("", request.password)
    }

    @Test
    fun loginRequest_handlesSpecialCharacters_inPassword() {
        val complexPassword = "P@ssw0rd!#\$%^&*()"
        val request = LoginRequest("user@domain.com", complexPassword)

        assertEquals("user@domain.com", request.email)
        assertEquals(complexPassword, request.password)
    }

    @Test
    fun loginRequest_equality_worksCorrectly() {
        val request1 = LoginRequest("user@example.com", "pass123")
        val request2 = LoginRequest("user@example.com", "pass123")
        val request3 = LoginRequest("other@example.com", "pass123")

        assertEquals(request1, request2)
        assertNotEquals(request1, request3)
    }

    @Test
    fun loginRequest_hashCode_isConsistent_forEqualObjects() {
        val request1 = LoginRequest("test@test.com", "password")
        val request2 = LoginRequest("test@test.com", "password")

        assertEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun loginRequest_copy_createsNewInstance() {
        val original = LoginRequest("original@test.com", "original123")
        val copy = original.copy()

        assertEquals(original, copy)
        assertEquals(original.email, copy.email)
        assertEquals(original.password, copy.password)
    }

    @Test
    fun loginRequest_canModify_withCopy() {
        val original = LoginRequest("old@test.com", "oldpass")
        val modified = original.copy(email = "new@test.com")

        assertEquals("new@test.com", modified.email)
        assertEquals("oldpass", modified.password)
        assertNotEquals(original, modified)
    }

    @Test
    fun loginRequest_handlesLongStrings() {
        val longEmail = "a".repeat(1000) + "@example.com"
        val longPassword = "P".repeat(1000)
        val request = LoginRequest(longEmail, longPassword)

        assertEquals(longEmail, request.email)
        assertEquals(longPassword, request.password)
    }

    @Test
    fun loginRequest_handlesUnicodeCharacters() {
        val unicodeEmail = "test@例え.com"
        val unicodePassword = "пароль123"
        val request = LoginRequest(unicodeEmail, unicodePassword)

        assertEquals(unicodeEmail, request.email)
        assertEquals(unicodePassword, request.password)
    }

    // ========== ScheduleResponse Tests ==========

    @Test
    fun scheduleResponse_canBeCreated_withAllFields() {
        val response = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 2,
            DateUtc = "2025-01-15T10:00:00Z",
            Location = "Stadium A",
            HomeTeam = "Team Alpha",
            AwayTeam = "Team Beta",
            Group = "Group A",
            HomeTeamScore = 3,
            AwayTeamScore = 2
        )

        assertEquals(1, response.MatchNumber)
        assertEquals(2, response.RoundNumber)
        assertEquals("2025-01-15T10:00:00Z", response.DateUtc)
        assertEquals("Stadium A", response.Location)
        assertEquals("Team Alpha", response.HomeTeam)
        assertEquals("Team Beta", response.AwayTeam)
        assertEquals("Group A", response.Group)
        assertEquals(3, response.HomeTeamScore)
        assertEquals(2, response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_handlesNullableFields_whenNull() {
        val response = ScheduleResponse(
            MatchNumber = 5,
            RoundNumber = 1,
            DateUtc = "2025-02-20T15:30:00Z",
            Location = "Arena B",
            HomeTeam = "Team Gamma",
            AwayTeam = "Team Delta",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )

        assertNull(response.Group)
        assertNull(response.HomeTeamScore)
        assertNull(response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_handlesNullableFields_whenPresent() {
        val response = ScheduleResponse(
            MatchNumber = 10,
            RoundNumber = 3,
            DateUtc = "2025-03-25T20:00:00Z",
            Location = "Field C",
            HomeTeam = "Team Epsilon",
            AwayTeam = "Team Zeta",
            Group = "Final",
            HomeTeamScore = 1,
            AwayTeamScore = 1
        )

        assertNotNull(response.Group)
        assertNotNull(response.HomeTeamScore)
        assertNotNull(response.AwayTeamScore)
        assertEquals("Final", response.Group)
        assertEquals(1, response.HomeTeamScore)
        assertEquals(1, response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_equality_worksCorrectly() {
        val response1 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2025-01-01T00:00:00Z",
            Location = "Venue",
            HomeTeam = "Home",
            AwayTeam = "Away",
            Group = "A",
            HomeTeamScore = 0,
            AwayTeamScore = 0
        )
        val response2 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2025-01-01T00:00:00Z",
            Location = "Venue",
            HomeTeam = "Home",
            AwayTeam = "Away",
            Group = "A",
            HomeTeamScore = 0,
            AwayTeamScore = 0
        )
        val response3 = ScheduleResponse(
            MatchNumber = 2, // Different
            RoundNumber = 1,
            DateUtc = "2025-01-01T00:00:00Z",
            Location = "Venue",
            HomeTeam = "Home",
            AwayTeam = "Away",
            Group = "A",
            HomeTeamScore = 0,
            AwayTeamScore = 0
        )

        assertEquals(response1, response2)
        assertNotEquals(response1, response3)
    }

    @Test
    fun scheduleResponse_copy_createsNewInstance() {
        val original = ScheduleResponse(
            MatchNumber = 7,
            RoundNumber = 2,
            DateUtc = "2025-04-10T12:00:00Z",
            Location = "Park",
            HomeTeam = "Tigers",
            AwayTeam = "Lions",
            Group = "B",
            HomeTeamScore = 2,
            AwayTeamScore = 3
        )
        val copy = original.copy()

        assertEquals(original, copy)
    }

    @Test
    fun scheduleResponse_canModify_withCopy() {
        val original = ScheduleResponse(
            MatchNumber = 15,
            RoundNumber = 4,
            DateUtc = "2025-05-05T18:00:00Z",
            Location = "Court",
            HomeTeam = "Bears",
            AwayTeam = "Wolves",
            Group = "C",
            HomeTeamScore = 1,
            AwayTeamScore = 2
        )
        val modified = original.copy(HomeTeamScore = 3, AwayTeamScore = 3)

        assertEquals(3, modified.HomeTeamScore)
        assertEquals(3, modified.AwayTeamScore)
        assertEquals(original.MatchNumber, modified.MatchNumber)
        assertNotEquals(original, modified)
    }

    @Test
    fun scheduleResponse_handlesZeroScores() {
        val response = ScheduleResponse(
            MatchNumber = 20,
            RoundNumber = 1,
            DateUtc = "2025-06-01T09:00:00Z",
            Location = "Stadium",
            HomeTeam = "TeamA",
            AwayTeam = "TeamB",
            Group = "D",
            HomeTeamScore = 0,
            AwayTeamScore = 0
        )

        assertEquals(0, response.HomeTeamScore)
        assertEquals(0, response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_handlesNegativeMatchNumbers() {
        // While not ideal, data classes should handle any valid Int
        val response = ScheduleResponse(
            MatchNumber = -1,
            RoundNumber = -1,
            DateUtc = "Invalid",
            Location = "Unknown",
            HomeTeam = "TBD",
            AwayTeam = "TBD",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )

        assertEquals(-1, response.MatchNumber)
        assertEquals(-1, response.RoundNumber)
    }

    @Test
    fun scheduleResponse_handlesLargeScores() {
        val response = ScheduleResponse(
            MatchNumber = 100,
            RoundNumber = 10,
            DateUtc = "2025-07-01T00:00:00Z",
            Location = "BigVenue",
            HomeTeam = "Scorers",
            AwayTeam = "Defenders",
            Group = "Champions",
            HomeTeamScore = 999,
            AwayTeamScore = 1000
        )

        assertEquals(999, response.HomeTeamScore)
        assertEquals(1000, response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_handlesEmptyStrings() {
        val response = ScheduleResponse(
            MatchNumber = 0,
            RoundNumber = 0,
            DateUtc = "",
            Location = "",
            HomeTeam = "",
            AwayTeam = "",
            Group = "",
            HomeTeamScore = 0,
            AwayTeamScore = 0
        )

        assertEquals("", response.DateUtc)
        assertEquals("", response.Location)
        assertEquals("", response.HomeTeam)
        assertEquals("", response.AwayTeam)
        assertEquals("", response.Group)
    }

    @Test
    fun scheduleResponse_handlesSpecialCharacters_inTeamNames() {
        val response = ScheduleResponse(
            MatchNumber = 42,
            RoundNumber = 5,
            DateUtc = "2025-08-15T16:45:00Z",
            Location = "São Paulo",
            HomeTeam = "FC Köln",
            AwayTeam = "Atlético",
            Group = "Group Ñ",
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )

        assertEquals("São Paulo", response.Location)
        assertEquals("FC Köln", response.HomeTeam)
        assertEquals("Atlético", response.AwayTeam)
        assertEquals("Group Ñ", response.Group)
    }

    @Test
    fun scheduleResponse_nullEquality_handledCorrectly() {
        val response1 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "Date",
            Location = "Loc",
            HomeTeam = "A",
            AwayTeam = "B",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )
        val response2 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "Date",
            Location = "Loc",
            HomeTeam = "A",
            AwayTeam = "B",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )

        assertEquals(response1, response2)
    }

    @Test
    fun scheduleResponse_mixedNullability_affectsEquality() {
        val response1 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "Date",
            Location = "Loc",
            HomeTeam = "A",
            AwayTeam = "B",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )
        val response2 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "Date",
            Location = "Loc",
            HomeTeam = "A",
            AwayTeam = "B",
            Group = "Group",
            HomeTeamScore = 1,
            AwayTeamScore = 2
        )

        assertNotEquals(response1, response2)
    }
}
