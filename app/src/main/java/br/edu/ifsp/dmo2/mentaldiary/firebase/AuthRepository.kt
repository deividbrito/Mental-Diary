package br.edu.ifsp.dmo2.mentaldiary.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    fun currentUser() = auth.currentUser

    suspend fun register(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) cont.resume(Result.success(auth.currentUser!!))
                    else cont.resume(Result.failure(task.exception ?: Exception("Erro desconhecido")))
                }
        }

    suspend fun login(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) cont.resume(Result.success(auth.currentUser!!))
                    else cont.resume(Result.failure(task.exception ?: Exception("Erro login")))
                }
        }

    fun logout() = auth.signOut()
}
