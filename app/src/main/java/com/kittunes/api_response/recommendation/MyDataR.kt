package com.kittunes.api_response.recommendation

import android.os.Parcel
import android.os.Parcelable

data class MyDataR(
    val data: List<DataR> = emptyList(),
    val next: String? = null,
    val total: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(DataR.CREATOR) ?: emptyList(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(data)
        parcel.writeString(next)
        parcel.writeInt(total)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MyDataR> {
        override fun createFromParcel(parcel: Parcel): MyDataR = MyDataR(parcel)
        override fun newArray(size: Int): Array<MyDataR?> = arrayOfNulls(size)
    }
}