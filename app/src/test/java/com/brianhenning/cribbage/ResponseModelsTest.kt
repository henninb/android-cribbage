package com.brianhenning.cribbage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for response data models.
 * Tests LoginResponse, ScheduleResponse, and LoginRequest data classes
 * to ensure proper serialization, equality, and null handling.
 */
class ResponseModelsTest {

    // ========== LoginResponse Tests ==========

    @Test
    fun loginResponse_creation_setsTokenCorrectly() {
        val response = LoginResponse(token = "abc123xyz")
        assertEquals("abc123xyz", response.token)
    }

    @Test
    fun loginResponse_equality_sameToken_areEqual() {
        val response1 = LoginResponse(token = "token123")
        val response2 = LoginResponse(token = "token123")
        assertEquals(response1, response2)
    }

    @Test
    fun loginResponse_equality_differentToken_areNotEqual() {
        val response1 = LoginResponse(token = "token123")
        val response2 = LoginResponse(token = "token456")
        assertNotEquals(response1, response2)
    }

    @Test
    fun loginResponse_hashCode_sameToken_haveSameHashCode() {
        val response1 = LoginResponse(token = "token123")
        val response2 = LoginResponse(token = "token123")
        assertEquals(response1.hashCode(), response2.hashCode())
    }

    @Test
    fun loginResponse_copy_createsNewInstance() {
        val original = LoginResponse(token = "original")
        val copied = original.copy()
        assertEquals(original, copied)
        assertEquals(original.token, copied.token)
    }

    @Test
    fun loginResponse_copy_withChangedToken_createsDifferentInstance() {
        val original = LoginResponse(token = "original")
        val modified = original.copy(token = "modified")
        assertEquals("modified", modified.token)
        assertNotEquals(original, modified)
    }

    @Test
    fun loginResponse_emptyToken_handlesCorrectly() {
        val response = LoginResponse(token = "")
        assertEquals("", response.token)
    }

    // ========== LoginRequest Tests ==========

    @Test
    fun loginRequest_creation_setsFieldsCorrectly() {
        val request = LoginRequest(
            email = "user@example.com",
            password = "secret123"
        )
        assertEquals("user@example.com", request.email)
        assertEquals("secret123", request.password)
    }

    @Test
    fun loginRequest_equality_sameCredentials_areEqual() {
        val request1 = LoginRequest("user@test.com", "pass123")
        val request2 = LoginRequest("user@test.com", "pass123")
        assertEquals(request1, request2)
    }

    @Test
    fun loginRequest_equality_differentEmail_areNotEqual() {
        val request1 = LoginRequest("user1@test.com", "pass123")
        val request2 = LoginRequest("user2@test.com", "pass123")
        assertNotEquals(request1, request2)
    }

    @Test
    fun loginRequest_equality_differentPassword_areNotEqual() {
        val request1 = LoginRequest("user@test.com", "pass123")
        val request2 = LoginRequest("user@test.com", "pass456")
        assertNotEquals(request1, request2)
    }

    @Test
    fun loginRequest_copy_createsNewInstance() {
        val original = LoginRequest("original@test.com", "password")
        val copied = original.copy()
        assertEquals(original, copied)
    }

    @Test
    fun loginRequest_copy_withChangedEmail_modifiesCorrectly() {
        val original = LoginRequest("old@test.com", "password")
        val modified = original.copy(email = "new@test.com")
        assertEquals("new@test.com", modified.email)
        assertEquals("password", modified.password)
    }

    // ========== ScheduleResponse Tests ==========

    @Test
    fun scheduleResponse_creation_setsAllFieldsCorrectly() {
        val response = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 2,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Xcel Energy Center",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 3,
            AwayTeamScore = 2
        )

        assertEquals(1, response.MatchNumber)
        assertEquals(2, response.RoundNumber)
        assertEquals("2024-10-01T19:00:00Z", response.DateUtc)
        assertEquals("Xcel Energy Center", response.Location)
        assertEquals("Wild", response.HomeTeam)
        assertEquals("Jets", response.AwayTeam)
        assertNull(response.Group)
        assertEquals(3, response.HomeTeamScore)
        assertEquals(2, response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_withNullScores_handlesCorrectly() {
        val response = ScheduleResponse(
            MatchNumber = 5,
            RoundNumber = 1,
            DateUtc = "2024-10-15T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Bruins",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )

        assertNull(response.HomeTeamScore)
        assertNull(response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_withNullGroup_handlesCorrectly() {
        val response = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )

        assertNull(response.Group)
    }

    @Test
    fun scheduleResponse_withGroup_setsCorrectly() {
        val response = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = "Group A",
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )

        assertEquals("Group A", response.Group)
    }

    @Test
    fun scheduleResponse_equality_sameData_areEqual() {
        val response1 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )
        val response2 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )

        assertEquals(response1, response2)
    }

    @Test
    fun scheduleResponse_equality_differentMatchNumber_areNotEqual() {
        val response1 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )
        val response2 = response1.copy(MatchNumber = 2)

        assertNotEquals(response1, response2)
    }

    @Test
    fun scheduleResponse_equality_differentScores_areNotEqual() {
        val response1 = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )
        val response2 = response1.copy(HomeTeamScore = 3)

        assertNotEquals(response1, response2)
    }

    @Test
    fun scheduleResponse_copy_createsNewInstance() {
        val original = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 2,
            AwayTeamScore = 1
        )
        val copied = original.copy()

        assertEquals(original, copied)
    }

    @Test
    fun scheduleResponse_copy_withModifiedTeams_updatesCorrectly() {
        val original = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )
        val modified = original.copy(
            HomeTeam = "Bruins",
            AwayTeam = "Maple Leafs"
        )

        assertEquals("Bruins", modified.HomeTeam)
        assertEquals("Maple Leafs", modified.AwayTeam)
        assertEquals(1, modified.MatchNumber) // Other fields unchanged
    }

    @Test
    fun scheduleResponse_copy_updatingScores_handlesCorrectly() {
        val original = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = null,
            AwayTeamScore = null
        )
        val modified = original.copy(
            HomeTeamScore = 4,
            AwayTeamScore = 3
        )

        assertEquals(4, modified.HomeTeamScore)
        assertEquals(3, modified.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_zeroScores_handlesCorrectly() {
        val response = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 0,
            AwayTeamScore = 0
        )

        assertEquals(0, response.HomeTeamScore)
        assertEquals(0, response.AwayTeamScore)
    }

    @Test
    fun scheduleResponse_highScores_handlesCorrectly() {
        val response = ScheduleResponse(
            MatchNumber = 1,
            RoundNumber = 1,
            DateUtc = "2024-10-01T19:00:00Z",
            Location = "Arena",
            HomeTeam = "Wild",
            AwayTeam = "Jets",
            Group = null,
            HomeTeamScore = 10,
            AwayTeamScore = 8
        )

        assertEquals(10, response.HomeTeamScore)
        assertEquals(8, response.AwayTeamScore)
    }
}
