package br.edu.ifsp.dmo2.mentaldiary.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import java.util.*

data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val texto: String = "",
    val humor: String = "",
    val dataCriacao: com.google.firebase.Timestamp? = null,
    val imagemUrl: String? = null,
    val foiPorVoz: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        userId = parcel.readString() ?: "",
        texto = parcel.readString() ?: "",
        humor = parcel.readString() ?: "",
        dataCriacao = parcel.readLong().let { if (it != -1L) Timestamp(Date(it)) else null },
        imagemUrl = parcel.readString(),
        foiPorVoz = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(texto)
        parcel.writeString(humor)
        parcel.writeLong(dataCriacao?.toDate()?.time ?: -1L) // -1 indica "null"
        parcel.writeString(imagemUrl)
        parcel.writeByte(if (foiPorVoz) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DiaryEntry> {
        override fun createFromParcel(parcel: Parcel): DiaryEntry = DiaryEntry(parcel)
        override fun newArray(size: Int): Array<DiaryEntry?> = arrayOfNulls(size)
    }
}