package com.kittunes.Api_Data

data class MyData(
    val `data`: List<Data>,
    val next: String,
    val total: Int
)