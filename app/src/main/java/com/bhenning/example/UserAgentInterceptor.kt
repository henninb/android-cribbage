package com.bhenning.example
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        Log.i("UserAgentInterceptor", "UA interceptor called.")
        println("UA interceptor called.")
        return chain.proceed(requestWithUserAgent)
    }
}