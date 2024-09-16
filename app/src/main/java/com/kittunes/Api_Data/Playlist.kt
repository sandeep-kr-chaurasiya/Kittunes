package com.kittunes.Api_Data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Playlist(
    val playlistId: String? = null,
    val playlistName: String? = null,
    val createdAt: Timestamp? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(playlistId)
        parcel.writeString(playlistName)
        parcel.writeParcelable(createdAt, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }
}