package com.bhenning.example

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @POST("/api/login")
    @Headers("x-px-block: 1", "x-px-mobile: 1")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}