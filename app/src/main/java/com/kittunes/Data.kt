package com.kittunes

import com.google.gson.annotations.SerializedName


data class Data (

  @SerializedName("uri"         ) var uri         : String? = null,
  @SerializedName("id"          ) var id          : String? = null,
  @SerializedName("displayName" ) var displayName : String? = null,
  @SerializedName("username"    ) var username    : String? = null,
  @SerializedName("image"       ) var image       : Image?  = Image()

)