package com.kittunes

import com.google.gson.annotations.SerializedName


data class Profile (

  @SerializedName("name" ) var name : String? = null

)