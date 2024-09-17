package com.kittunes.Api_Data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Song(
    val songId: String? = null,          // Unique ID for the song
    val title: String? = null,           // Title of the song
    val artist: String? = null,          // Artist of the song
    val album: String? = null,           // Album name
    val coverUrl: String? = null,        // URL for the cover image (optional)
    val releaseDate: Timestamp? = null   // Release date of the song (optional)
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