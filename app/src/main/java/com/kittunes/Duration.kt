package com.kittunes

import com.google.gson.annotations.SerializedName


data class Duration (

  @SerializedName("totalMilliseconds" ) var totalMilliseconds : Int? = null

)