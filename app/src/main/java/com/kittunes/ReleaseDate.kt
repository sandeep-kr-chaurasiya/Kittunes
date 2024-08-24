package com.kittunes

import com.google.gson.annotations.SerializedName


data class ReleaseDate (

  @SerializedName("isoString" ) var isoString : String? = null

)