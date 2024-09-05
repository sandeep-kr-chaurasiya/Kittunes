package com.kittunes.Api

import com.kittunes.Api_Data.MyData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers(
        "X-RapidAPI-Key: e022cda6c5msha02364a65628af3p118be3jsna1e310eb24f0",
        "X-RapidAPI-Host: deezerdevs-deezer.p.rapidapi.com"
    )
    @GET("/search")
    fun getdata(@Query("q") query: String): Call<MyData>
}