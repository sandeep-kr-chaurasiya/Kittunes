package com.kittunes.Api_Data

import android.os.Parcel
import android.os.Parcelable

data class MyData(
    val data: List<Data> = emptyList(),
    val next: String? = null,
    val total: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Data.CREATOR) ?: emptyList(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(data)
        parcel.writeString(next)
        parcel.writeInt(total)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MyData> {
        override fun createFromParcel(parcel: Parcel): MyData = MyData(parcel)
        override fun newArray(size: Int): Array<MyData?> = arrayOfNulls(size)
    }
}