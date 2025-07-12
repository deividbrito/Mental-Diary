package br.edu.ifsp.dmo2.mentaldiary.firebase

import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import br.edu.ifsp.dmo2.mentaldiary.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")
    private val entries = db.collection("entries")

    fun createUserProfile(user: FirebaseUser, nome: String) {
        val profile = User(
            uid  = user.uid,
            nome = nome,
            email = user.email ?: "",
            dataCriacao = Timestamp.now()
        )
        users.document(user.uid).set(profile)
    }

    fun addEntry(entry: DiaryEntry) =
        entries.add(entry)

    fun getEntriesForUser(uid: String,
                          limit: Long = 10,
                          startAfter: DocumentSnapshot? = null
    ): Query {
        var q: Query = entries
            .whereEqualTo("userId", uid)
            .orderBy("dataCriacao", Query.Direction.DESCENDING)
            .limit(limit)
        if (startAfter != null) q = q.startAfter(startAfter)
        return q
    }

    fun updateEntry(entryId: String, data: Map<String, Any>) =
        entries.document(entryId).update(data)

    fun deleteEntry(entryId: String) =
        entries.document(entryId).delete()
}
