package com.kittunes

import com.google.gson.annotations.SerializedName


data class Artists (

  @SerializedName("items" ) var items : ArrayList<Items> = arrayListOf()

)