package com.kittunes

import com.google.gson.annotations.SerializedName


data class AlbumOfTrack (

  @SerializedName("uri"         ) var uri         : String?      = null,
  @SerializedName("name"        ) var name        : String?      = null,
  @SerializedName("coverArt"    ) var coverArt    : CoverArt?    = CoverArt(),
  @SerializedName("id"          ) var id          : String?      = null,
  @SerializedName("sharingInfo" ) var sharingInfo : SharingInfo? = SharingInfo()

)