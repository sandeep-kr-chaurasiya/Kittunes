package com.kittunes

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers(
        "x-rapidapi-host: spotify23.p.rapidapi.com",
        "x-rapidapi-key: e022cda6c5msha02364a65628af3p118be3jsna1e310eb24f0"
    )
    @GET("/search/")
    fun getData(@Query("q") query: String): Call<MyData>
}