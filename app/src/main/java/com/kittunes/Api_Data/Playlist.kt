package com.kittunes.Api_Data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Playlist(
    val playlistName: String = "",
    val createdAt: Timestamp? = null,
    val userId: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(playlistName)
        parcel.writeParcelable(createdAt, flags)
        parcel.writeString(userId)
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