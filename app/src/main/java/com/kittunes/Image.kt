package com.kittunes

import com.google.gson.annotations.SerializedName


data class Image (

  @SerializedName("smallImageUrl" ) var smallImageUrl : String? = null,
  @SerializedName("largeImageUrl" ) var largeImageUrl : String? = null

)