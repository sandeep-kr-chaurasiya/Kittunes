package com.kittunes.api_response.search

import android.os.Parcel
import android.os.Parcelable

data class Data(
    val album: Album,
    val artist: Artist,
    val duration: Int,
    val explicit_content_cover: Int,
    val explicit_content_lyrics: Int,
    val explicit_lyrics: Boolean,
    val id: Long,
    val link: String,
    val md5_image: String,
    val preview: String,
    val rank: Int,
    val readable: Boolean,
    val title: String,
    val title_short: String,
    val title_version: String,
    val type: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Album::class.java.classLoader) ?: Album("", "", "", "", "", 0, "", "", "", ""),
        parcel.readParcelable(Artist::class.java.classLoader) ?: Artist(0, "", "", "", "", "", "", "", "", ""),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(album, flags)
        parcel.writeParcelable(artist, flags)
        parcel.writeInt(duration)
        parcel.writeInt(explicit_content_cover)
        parcel.writeInt(explicit_content_lyrics)
        parcel.writeByte(if (explicit_lyrics) 1 else 0)
        parcel.writeLong(id)
        parcel.writeString(link)
        parcel.writeString(md5_image)
        parcel.writeString(preview)
        parcel.writeInt(rank)
        parcel.writeByte(if (readable) 1 else 0)
        parcel.writeString(title)
        parcel.writeString(title_short)
        parcel.writeString(title_version)
        parcel.writeString(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Data> {
        override fun createFromParcel(parcel: Parcel): Data = Data(parcel)
        override fun newArray(size: Int): Array<Data?> = arrayOfNulls(size)
    }
}