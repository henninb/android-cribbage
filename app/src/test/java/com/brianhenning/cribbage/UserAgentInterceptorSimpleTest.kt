package com.brianhenning.cribbage

import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Simple unit tests for UserAgentInterceptor that verify
 * the class can be instantiated with various inputs.
 */
class UserAgentInterceptorSimpleTest {

    @Test
    fun userAgentInterceptor_canBeInstantiated() {
        val interceptor = UserAgentInterceptor("TestApp/1.0")
        assertNotNull(interceptor)
    }

    @Test
    fun userAgentInterceptor_acceptsEmptyString() {
        val interceptor = UserAgentInterceptor("")
        assertNotNull(interceptor)
    }

    @Test
    fun userAgentInterceptor_acceptsLongString() {
        val longUserAgent = "A".repeat(1000)
        val interceptor = UserAgentInterceptor(longUserAgent)
        assertNotNull(interceptor)
    }

    @Test
    fun userAgentInterceptor_acceptsSpecialCharacters() {
        val specialUserAgent = "App/1.0 (Android; SDK 34; en-US)"
        val interceptor = UserAgentInterceptor(specialUserAgent)
        assertNotNull(interceptor)
    }

    @Test
    fun userAgentInterceptor_acceptsUnicodeCharacters() {
        val unicodeUserAgent = "测试App/1.0"
        val interceptor = UserAgentInterceptor(unicodeUserAgent)
        assertNotNull(interceptor)
    }
}
