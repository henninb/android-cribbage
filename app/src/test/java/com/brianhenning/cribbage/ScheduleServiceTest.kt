package com.brianhenning.cribbage

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScheduleServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var service: ScheduleService

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

        service = retrofit.create(ScheduleService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun schedule_success_parsesArray_andSendsHeaders() {
        val body = """
            [
              {
                "MatchNumber": 1,
                "RoundNumber": 1,
                "DateUtc": "2024-10-01T00:00:00Z",
                "Location": "Arena",
                "HomeTeam": "Wild",
                "AwayTeam": "Jets",
                "Group": null,
                "HomeTeamScore": null,
                "AwayTeamScore": null
              }
            ]
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
        )

        val response = service.schedule().execute()

        assertTrue(response.isSuccessful)
        val items = response.body() ?: emptyArray()
        assertEquals(1, items.size)
        val first = items[0]
        assertEquals(1, first.MatchNumber)
        assertEquals("Wild", first.HomeTeam)
        assertEquals("Jets", first.AwayTeam)

        val recorded = server.takeRequest()
        assertEquals("/feed/json/nhl-2023/minnesota-wild", recorded.path)
        assertEquals("GET", recorded.method)
        assertEquals("1", recorded.getHeader("x-px-block"))
        assertEquals("1", recorded.getHeader("x-px-mobile"))
    }

    @Test
    fun schedule_emptyArray_returnsEmptyList() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
        )

        val response = service.schedule().execute()

        assertTrue(response.isSuccessful)
        val items = response.body() ?: emptyArray()
        assertEquals(0, items.size)
    }

    @Test
    fun schedule_multipleGames_parsesAll() {
        val body = """
            [
              {
                "MatchNumber": 1,
                "RoundNumber": 1,
                "DateUtc": "2024-10-01T19:00:00Z",
                "Location": "Xcel Energy Center",
                "HomeTeam": "Wild",
                "AwayTeam": "Jets",
                "Group": null,
                "HomeTeamScore": 3,
                "AwayTeamScore": 2
              },
              {
                "MatchNumber": 2,
                "RoundNumber": 1,
                "DateUtc": "2024-10-03T19:00:00Z",
                "Location": "Canada Life Centre",
                "HomeTeam": "Jets",
                "AwayTeam": "Wild",
                "Group": null,
                "HomeTeamScore": 1,
                "AwayTeamScore": 4
              }
            ]
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
        )

        val response = service.schedule().execute()

        assertTrue(response.isSuccessful)
        val items = response.body() ?: emptyArray()
        assertEquals(2, items.size)

        val first = items[0]
        assertEquals(1, first.MatchNumber)
        assertEquals("Wild", first.HomeTeam)
        assertEquals(3, first.HomeTeamScore)
        assertEquals(2, first.AwayTeamScore)

        val second = items[1]
        assertEquals(2, second.MatchNumber)
        assertEquals("Jets", second.HomeTeam)
        assertEquals(1, second.HomeTeamScore)
        assertEquals(4, second.AwayTeamScore)
    }

    @Test
    fun schedule_withNullScores_parsesCorrectly() {
        val body = """
            [
              {
                "MatchNumber": 5,
                "RoundNumber": 2,
                "DateUtc": "2024-10-15T19:00:00Z",
                "Location": "Target Center",
                "HomeTeam": "Wild",
                "AwayTeam": "Bruins",
                "Group": null,
                "HomeTeamScore": null,
                "AwayTeamScore": null
              }
            ]
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
        )

        val response = service.schedule().execute()

        assertTrue(response.isSuccessful)
        val items = response.body() ?: emptyArray()
        assertEquals(1, items.size)
        val game = items[0]
        assertEquals(null, game.HomeTeamScore)
        assertEquals(null, game.AwayTeamScore)
    }

    @Test
    fun schedule_serverError_returnsUnsuccessful() {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        val response = service.schedule().execute()

        assertTrue(!response.isSuccessful)
        assertEquals(500, response.code())
    }

    @Test
    fun schedule_notFound_returnsUnsuccessful() {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("Not Found")
        )

        val response = service.schedule().execute()

        assertTrue(!response.isSuccessful)
        assertEquals(404, response.code())
    }
}
