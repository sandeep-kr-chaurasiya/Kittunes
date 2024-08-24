package com.kittunes

import com.google.gson.annotations.SerializedName


data class Images (

  @SerializedName("items" ) var items : ArrayList<Items> = arrayListOf()

)