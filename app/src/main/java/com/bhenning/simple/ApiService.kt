package com.bhenning.simple

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("your/api/endpoint")
    fun fetchData(): Call<ApiResponse>
}