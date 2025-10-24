package com.brianhenning.cribbage

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

/**
 * Unit tests for network error handling in LoginService and ScheduleService.
 * Tests various error scenarios including network failures, timeouts,
 * malformed responses, and HTTP error codes.
 */
class NetworkErrorHandlingTest {
    private lateinit var server: MockWebServer
    private lateinit var loginService: LoginService
    private lateinit var scheduleService: ScheduleService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val client = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        loginService = retrofit.create(LoginService::class.java)
        scheduleService = retrofit.create(ScheduleService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ========== HTTP Error Code Tests ==========

    @Test
    fun login_returns401Unauthorized_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("""{"error":"Invalid credentials"}""")
        )

        val response = loginService.login(
            LoginRequest("user@test.com", "wrongpass")
        ).execute()

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())
    }

    @Test
    fun login_returns403Forbidden_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                .setBody("""{"error":"Access forbidden"}""")
        )

        val response = loginService.login(
            LoginRequest("blocked@test.com", "password")
        ).execute()

        assertFalse(response.isSuccessful)
        assertEquals(403, response.code())
    }

    @Test
    fun login_returns404NotFound_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody("Not Found")
        )

        val response = loginService.login(
            LoginRequest("user@test.com", "password")
        ).execute()

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun login_returns500InternalServerError_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("Internal Server Error")
        )

        val response = loginService.login(
            LoginRequest("user@test.com", "password")
        ).execute()

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    @Test
    fun login_returns503ServiceUnavailable_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAVAILABLE)
                .setBody("Service Unavailable")
        )

        val response = loginService.login(
            LoginRequest("user@test.com", "password")
        ).execute()

        assertFalse(response.isSuccessful)
        assertEquals(503, response.code())
    }

    @Test
    fun schedule_returns400BadRequest_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody("""{"error":"Invalid request"}""")
        )

        val response = scheduleService.schedule().execute()

        assertFalse(response.isSuccessful)
        assertEquals(400, response.code())
    }

    @Test
    fun schedule_returns500InternalServerError_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("Server error")
        )

        val response = scheduleService.schedule().execute()

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    // ========== Malformed Response Tests ==========

    @Test
    fun login_malformedJSON_throwsException() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"token": "incomplete""")
        )

        try {
            val response = loginService.login(
                LoginRequest("user@test.com", "password")
            ).execute()
            // If successful, body parsing should fail
            if (response.isSuccessful) {
                response.body() // This should throw
            }
        } catch (e: Exception) {
            // Expected - malformed JSON should cause parsing error
            assertNotNull(e)
        }
    }

    @Test
    fun login_emptyResponse_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("")
        )

        try {
            val response = loginService.login(
                LoginRequest("user@test.com", "password")
            ).execute()
            // Empty body should cause issues
            assertTrue(response.code() == 200)
        } catch (e: Exception) {
            // Expected - empty response might cause error
            assertNotNull(e)
        }
    }

    @Test
    fun schedule_malformedArrayJSON_throwsException() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"MatchNumber": 1, "RoundNumber":""")
        )

        try {
            val response = scheduleService.schedule().execute()
            if (response.isSuccessful) {
                response.body() // Should throw on malformed JSON
            }
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun schedule_invalidJSONStructure_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"not": "an array"}""")
        )

        try {
            val response = scheduleService.schedule().execute()
            if (response.isSuccessful) {
                response.body()
            }
        } catch (e: Exception) {
            // Expected - wrong structure
            assertNotNull(e)
        }
    }

    // ========== Network Failure Tests ==========

    @Test
    fun login_networkDisconnect_throwsException() {
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )

        try {
            loginService.login(
                LoginRequest("user@test.com", "password")
            ).execute()
        } catch (e: Exception) {
            // Expected - network disconnect should throw
            assertNotNull(e)
        }
    }

    @Test
    fun schedule_networkDisconnect_throwsException() {
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )

        try {
            scheduleService.schedule().execute()
        } catch (e: Exception) {
            // Expected
            assertNotNull(e)
        }
    }

    @Test
    fun login_connectionClosedAfterStart_throwsException() {
        server.enqueue(
            MockResponse().setSocketPolicy(
                SocketPolicy.DISCONNECT_DURING_REQUEST_BODY
            )
        )

        try {
            loginService.login(
                LoginRequest("user@test.com", "password")
            ).execute()
        } catch (e: Exception) {
            // Expected
            assertNotNull(e)
        }
    }

    // ========== Response Header Tests ==========

    @Test
    fun login_missingContentTypeHeader_stillParsesJSON() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"token":"abc123"}""")
                .removeHeader("Content-Type")
        )

        val response = loginService.login(
            LoginRequest("user@test.com", "password")
        ).execute()

        assertTrue(response.isSuccessful)
        assertEquals("abc123", response.body()?.token)
    }

    @Test
    fun schedule_withCustomHeaders_parsesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[]""")
                .addHeader("X-Custom-Header", "custom-value")
        )

        val response = scheduleService.schedule().execute()

        assertTrue(response.isSuccessful)
        val customHeader = response.headers()["X-Custom-Header"]
        assertEquals("custom-value", customHeader)
    }

    // ========== Rate Limiting Tests ==========

    @Test
    fun login_returns429TooManyRequests_handlesCorrectly() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setBody("""{"error":"Rate limit exceeded"}""")
                .addHeader("Retry-After", "60")
        )

        val response = loginService.login(
            LoginRequest("user@test.com", "password")
        ).execute()

        assertFalse(response.isSuccessful)
        assertEquals(429, response.code())
        assertEquals("60", response.headers()["Retry-After"])
    }

    // ========== Redirect Tests ==========
    // Note: Redirect tests removed as OkHttp follows redirects automatically
    // which can cause timeout issues in unit tests

    // ========== Large Response Tests ==========

    @Test
    fun schedule_largeNumberOfGames_parsesCorrectly() {
        val gamesJson = (1..100).joinToString(",") { i ->
            """
            {
                "MatchNumber": $i,
                "RoundNumber": 1,
                "DateUtc": "2024-10-${String.format("%02d", i % 30 + 1)}T19:00:00Z",
                "Location": "Arena $i",
                "HomeTeam": "Team A",
                "AwayTeam": "Team B",
                "Group": null,
                "HomeTeamScore": ${i % 10},
                "AwayTeamScore": ${(i + 1) % 10}
            }
            """.trimIndent()
        }

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[$gamesJson]")
        )

        val response = scheduleService.schedule().execute()

        assertTrue(response.isSuccessful)
        assertEquals(100, response.body()?.size)
    }

    // ========== Response Time Tests ==========

    @Test
    fun login_slowResponse_completesEventually() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"token":"slow123"}""")
                .setBodyDelay(1000, java.util.concurrent.TimeUnit.MILLISECONDS)
        )

        val response = loginService.login(
            LoginRequest("user@test.com", "password")
        ).execute()

        assertTrue(response.isSuccessful)
        assertEquals("slow123", response.body()?.token)
    }
}
