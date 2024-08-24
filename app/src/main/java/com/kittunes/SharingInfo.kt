package com.kittunes

import com.google.gson.annotations.SerializedName


data class SharingInfo (

  @SerializedName("shareUrl" ) var shareUrl : String? = null

)