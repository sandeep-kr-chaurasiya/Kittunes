package com.kittunes

import com.google.gson.annotations.SerializedName


data class Featured (

  @SerializedName("data" ) var data : Data? = Data()

)