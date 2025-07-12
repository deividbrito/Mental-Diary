package br.edu.ifsp.dmo2.mentaldiary.model

data class User(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val dataCriacao: com.google.firebase.Timestamp? = null
)