package com.kittunes

import com.google.gson.annotations.SerializedName


data class Playability (

  @SerializedName("playable" ) var playable : Boolean? = null

)