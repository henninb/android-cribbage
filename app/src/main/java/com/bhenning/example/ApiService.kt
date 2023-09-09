package com.bhenning.example

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("api/login")
    fun fetchData(): Call<ApiResponse>
}