package br.edu.ifsp.dmo2.mentaldiary.model

data class DiaryEntry(
    val id: String = "",
    val userId: String = "",
    val texto: String = "",
    val humor: String = "",
    val dataCriacao: com.google.firebase.Timestamp? = null,
    val imagemUrl: String? = null,
    val foiPorVoz: Boolean = false
)