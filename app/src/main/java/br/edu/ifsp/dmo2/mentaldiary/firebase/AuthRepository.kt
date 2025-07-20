package br.edu.ifsp.dmo2.mentaldiary.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository {
    // instancia da autenticação do Firebase
    private val auth = FirebaseAuth.getInstance()

    // retorna o usuário atualmente logado, se houver
    fun currentUser() = auth.currentUser

    // funcao suspensa para registrar um novo usuario com email e senha
    suspend fun register(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            // tenta criar um novo usuário com o Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    // se sucesso, retorna o usuário atual autenticado
                        cont.resume(Result.success(auth.currentUser!!))
                    else
                    // se falha, retorna o erro encapsulado
                        cont.resume(Result.failure(task.exception ?: Exception("Erro desconhecido")))
                }
        }

    // funcao suspensa para login com email e senha
    suspend fun login(email: String, password: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            // autentica o usuario no Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        cont.resume(Result.success(auth.currentUser!!))
                    else
                        cont.resume(Result.failure(task.exception ?: Exception("Erro login")))
                }
        }

    // realiza logout do usuario atual
    fun logout() = auth.signOut()
}