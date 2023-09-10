package com.bhenning.example

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

//  curl -X POST https://cflare.bhenning.com/api/login -H "Content-Type: application/json" -d '{"email":"henninb@gmail.com", "password":"monday1"}'
//interface ApiService {
//    @POST("api/login")
//    fun fetchData(): Call<ApiResponse>
//}

interface LoginService {
    @POST("api/login")
    fun login(@Body loginRequest: LoginRequest): Call<String>
}