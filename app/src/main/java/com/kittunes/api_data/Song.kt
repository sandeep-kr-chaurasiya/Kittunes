package com.kittunes.api_data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Song(
    val songId: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val coverUrl: String? = null,
    val releaseDate: Timestamp? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songId)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeString(coverUrl)
        parcel.writeParcelable(releaseDate, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}