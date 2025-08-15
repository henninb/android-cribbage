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

class LoginServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var service: LoginService

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

        service = retrofit.create(LoginService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_success_parsesToken_andSendsHeaders() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"token":"abc123"}""")
        )

        val response = service.login(LoginRequest("user@example.com", "secret")).execute()

        assertTrue(response.isSuccessful)
        assertEquals("abc123", response.body()?.token)

        val recorded = server.takeRequest()
        assertEquals("/api/login", recorded.path)
        assertEquals("POST", recorded.method)
        // Headers from Retrofit @Headers annotation
        assertEquals("1", recorded.getHeader("x-px-block"))
        assertEquals("1", recorded.getHeader("x-px-mobile"))
    }
}
