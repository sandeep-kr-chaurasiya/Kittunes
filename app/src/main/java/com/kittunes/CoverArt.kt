package com.kittunes

import com.google.gson.annotations.SerializedName


data class CoverArt (

  @SerializedName("sources" ) var sources : ArrayList<Sources> = arrayListOf()

)