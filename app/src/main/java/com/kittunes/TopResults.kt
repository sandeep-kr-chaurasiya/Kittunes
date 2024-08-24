package com.kittunes

import com.google.gson.annotations.SerializedName


data class TopResults (

  @SerializedName("items"    ) var items    : ArrayList<Items>    = arrayListOf(),
  @SerializedName("featured" ) var featured : ArrayList<Featured> = arrayListOf()

)