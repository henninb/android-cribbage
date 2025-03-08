package com.brianhenning.cribbage

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

//  curl -X POST https://cflare.bhenning.com/api/login -H "Content-Type: application/json" -d '{"email":"henninb@gmail.com", "password":"monday1"}'
// curl https://fixturedownload.com/feed/json/nhl-2023/minnesota-wild

interface ScheduleService {
    @Headers("x-px-block: 1", "x-px-mobile: 1")
    @GET("feed/json/nhl-2023/minnesota-wild")
    fun schedule() : Call<Array<ScheduleResponse>>
}