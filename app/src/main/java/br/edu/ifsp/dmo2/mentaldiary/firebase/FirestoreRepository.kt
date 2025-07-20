package br.edu.ifsp.dmo2.mentaldiary.firebase

import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import br.edu.ifsp.dmo2.mentaldiary.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreRepository {

    // instancia do banco Firestore
    private val db = FirebaseFirestore.getInstance()

    // referencias para as colecoes "users" e "entries"
    private val users = db.collection("users")
    private val entries = db.collection("entries")

    // cria um perfil de usuario na colecao "users"
    fun createUserProfile(user: FirebaseUser, nome: String) {
        val profile = User(
            uid = user.uid,
            nome = nome,
            email = user.email ?: "",
            dataCriacao = Timestamp.now()  // salva a data de criacao
        )
        // salva o perfil com ID igual ao UID do Firebase
        users.document(user.uid).set(profile)
    }

    // adiciona uma nova entrada de diário a colecao "entries"
    fun addEntry(entry: DiaryEntry) =
        entries.add(entry)

    // retorna uma consulta com as entradas de um usuário específico
    fun getEntriesForUser(
        uid: String,
        limit: Long = 10,
        startAfter: DocumentSnapshot? = null
    ): Query {
        // cria a query base com filtro por userId e ordenação por data
        var q: Query = entries
            .whereEqualTo("userId", uid)
            .orderBy("dataCriacao", Query.Direction.DESCENDING)
            .limit(limit)

        // se estiver paginando, começa após o último documento carregado
        if (startAfter != null) q = q.startAfter(startAfter)

        return q
    }

    // atualiza os campos de uma entrada de diario com base no ID
    fun updateEntry(entryId: String, data: Map<String, Any>) =
        entries.document(entryId).update(data)

    // remove uma entrada de diario com base no ID
    fun deleteEntry(entryId: String) =
        entries.document(entryId).delete()
}
