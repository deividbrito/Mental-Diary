package br.edu.ifsp.dmo2.mentaldiary.test

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.dmo2.mentaldiary.R
import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import br.edu.ifsp.dmo2.mentaldiary.firebase.AuthRepository
import br.edu.ifsp.dmo2.mentaldiary.firebase.FirestoreRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class TestFirebaseActivity : AppCompatActivity() {

    // UI
    private lateinit var emailEt: EditText
    private lateinit var passEt: EditText
    private lateinit var nameEt: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var btnCreateProfile: Button
    private lateinit var btnAddEntry: Button
    private lateinit var btnListEntries: Button

    // Repositórios
    private val authRepo = AuthRepository()
    private val fsRepo = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_firebase)

        emailEt        = findViewById(R.id.et_email)
        passEt         = findViewById(R.id.et_pass)
        nameEt         = findViewById(R.id.et_name)
        btnRegister    = findViewById(R.id.btn_register)
        btnLogin       = findViewById(R.id.btn_login)
        btnCreateProfile = findViewById(R.id.btn_create_profile)
        btnAddEntry    = findViewById(R.id.btn_add_entry)
        btnListEntries = findViewById(R.id.btn_list_entries)

        btnRegister.setOnClickListener {
            lifecycleScope.launch {
                val res = authRepo.register(
                    emailEt.text.toString(),
                    passEt.text.toString()
                )
                res.onSuccess {
                    toast("cadastro OK: ${it.email}")
                }.onFailure {
                    toast("erro cadastro: ${it.message}")
                }
            }
        }

        btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val res = authRepo.login(
                    emailEt.text.toString(),
                    passEt.text.toString()
                )
                res.onSuccess {
                    toast("login OK: ${it.email}")
                }.onFailure {
                    toast("erro login: ${it.message}")
                }
            }
        }

        btnCreateProfile.setOnClickListener {
            val user = authRepo.currentUser()
            if (user == null) {
                toast("fazer login")
                return@setOnClickListener
            }
            fsRepo.createUserProfile(user, nameEt.text.toString())
            toast("criou/atualizou perfil")
        }

        btnAddEntry.setOnClickListener {
            val user = authRepo.currentUser()
            if (user == null) {
                toast("precisa logar")
                return@setOnClickListener
            }
            val entry = DiaryEntry(
                userId = user.uid,
                texto = "Entrada de teste (${System.currentTimeMillis()})",
                humor = "Feliz",
                dataCriacao = Timestamp.now(),
                foiPorVoz = false
            )
            fsRepo.addEntry(entry).addOnSuccessListener {
                toast("salvou entrada (id=${it.id})")
            }.addOnFailureListener {
                toast("erro pra salvar: ${it.message}")
            }
        }

        btnListEntries.setOnClickListener {
            val user = authRepo.currentUser()
            if (user == null) {
                toast("precisa logar")
                return@setOnClickListener
            }
            fsRepo.getEntriesForUser(user.uid).get()
                .addOnSuccessListener { snap ->
                    for (doc in snap.documents) {
                        val e = doc.toObject(DiaryEntry::class.java)
                        Log.d("TEST_FIRESTORE", "id=${doc.id}, texto=${e?.texto}")
                    }
                    toast("listagem ok - ver logcat")
                }
                .addOnFailureListener {
                    toast("erro: ${it.message}")
                }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
