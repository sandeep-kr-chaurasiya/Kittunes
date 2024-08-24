package com.kittunes

import com.google.gson.annotations.SerializedName


data class Tracks (

  @SerializedName("totalCount" ) var totalCount : Int?             = null,
  @SerializedName("items"      ) var items      : ArrayList<Items> = arrayListOf()

)