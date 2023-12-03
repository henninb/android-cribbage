package com.bhenning.example

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("api/login")
    fun login(@Body loginRequest: LoginResponse): Call<String>
}