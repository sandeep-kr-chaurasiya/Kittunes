package com.kittunes

import com.google.gson.annotations.SerializedName


data class AvatarImage (

  @SerializedName("sources" ) var sources : ArrayList<Sources> = arrayListOf()

)