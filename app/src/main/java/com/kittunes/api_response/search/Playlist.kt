package com.kittunes.api_response.search

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Playlist(
    val playlistId: String? = null,
    val playlistName: String? = null,
    val createdAt: Timestamp? = null,
    val songIds: List<String>? = null // List of song IDs in the playlist
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.createStringArrayList() // Read the songIds list from the Parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(playlistId)
        parcel.writeString(playlistName)
        parcel.writeParcelable(createdAt, flags)
        parcel.writeStringList(songIds) // Write the songIds list to the Parcel
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