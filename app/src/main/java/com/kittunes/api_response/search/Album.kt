package com.kittunes.api_response.search

import android.os.Parcel
import android.os.Parcelable

data class Album(
    val cover: String,
    val cover_big: String,
    val cover_medium: String,
    val cover_small: String,
    val cover_xl: String,
    val id: Int,
    val md5_image: String,
    val title: String,
    val tracklist: String,
    val type: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cover)
        parcel.writeString(cover_big)
        parcel.writeString(cover_medium)
        parcel.writeString(cover_small)
        parcel.writeString(cover_xl)
        parcel.writeInt(id)
        parcel.writeString(md5_image)
        parcel.writeString(title)
        parcel.writeString(tracklist)
        parcel.writeString(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album = Album(parcel)
        override fun newArray(size: Int): Array<Album?> = arrayOfNulls(size)
    }
}