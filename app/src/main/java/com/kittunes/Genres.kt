package com.kittunes

import com.google.gson.annotations.SerializedName


data class Genres (

  @SerializedName("totalCount" ) var totalCount : Int?              = null,
  @SerializedName("items"      ) var items      : ArrayList<String> = arrayListOf()

)