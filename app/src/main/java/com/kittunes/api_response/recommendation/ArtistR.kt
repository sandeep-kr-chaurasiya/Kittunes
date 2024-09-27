package com.kittunes.api_response.recommendation

import android.os.Parcel
import android.os.Parcelable

data class ArtistR(
    val id: Int,
    val link: String,
    val name: String,
    val picture: String,
    val picture_big: String,
    val picture_medium: String,
    val picture_small: String,
    val picture_xl: String,
    val tracklist: String,
    val type: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(link)
        parcel.writeString(name)
        parcel.writeString(picture)
        parcel.writeString(picture_big)
        parcel.writeString(picture_medium)
        parcel.writeString(picture_small)
        parcel.writeString(picture_xl)
        parcel.writeString(tracklist)
        parcel.writeString(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArtistR> {
        override fun createFromParcel(parcel: Parcel): ArtistR = ArtistR(parcel)
        override fun newArray(size: Int): Array<ArtistR?> = arrayOfNulls(size)
    }
}