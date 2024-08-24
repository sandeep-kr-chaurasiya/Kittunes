package com.kittunes

import com.google.gson.annotations.SerializedName


data class Podcast (

  @SerializedName("coverArt" ) var coverArt : CoverArt? = CoverArt()

)