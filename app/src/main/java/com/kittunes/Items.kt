package com.kittunes

import com.google.gson.annotations.SerializedName


data class Items (

  @SerializedName("data" ) var data : Data? = Data()

)