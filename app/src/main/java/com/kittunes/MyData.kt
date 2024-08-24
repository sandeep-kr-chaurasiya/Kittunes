package com.kittunes

import com.google.gson.annotations.SerializedName


data class MyData (

  @SerializedName("albums"     ) var albums     : Albums?     = Albums(),
  @SerializedName("artists"    ) var artists    : Artists?    = Artists(),
  @SerializedName("episodes"   ) var episodes   : Episodes?   = Episodes(),
  @SerializedName("genres"     ) var genres     : Genres?     = Genres(),
  @SerializedName("playlists"  ) var playlists  : Playlists?  = Playlists(),
  @SerializedName("podcasts"   ) var podcasts   : Podcasts?   = Podcasts(),
  @SerializedName("topResults" ) var topResults : TopResults? = TopResults(),
  @SerializedName("tracks"     ) var tracks     : Tracks?     = Tracks(),
  @SerializedName("users"      ) var users      : Users?      = Users()

)