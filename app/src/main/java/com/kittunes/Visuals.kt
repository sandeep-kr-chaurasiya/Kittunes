package com.kittunes

import com.google.gson.annotations.SerializedName


data class Visuals (

  @SerializedName("avatarImage" ) var avatarImage : AvatarImage? = AvatarImage()

)