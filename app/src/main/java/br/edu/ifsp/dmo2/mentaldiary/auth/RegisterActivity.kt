package br.edu.ifsp.dmo2.mentaldiary.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.mentaldiary.databinding.ActivityRegisterBinding
import br.edu.ifsp.dmo2.mentaldiary.firebase.AuthRepository
import br.edu.ifsp.dmo2.mentaldiary.firebase.FirestoreRepository
import br.edu.ifsp.dmo2.mentaldiary.ui.HomeActivity
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {

    // inicializa o binding da tela
    private lateinit var binding: ActivityRegisterBinding

    // instancia os repositorios de auth e firestore
    private val authRepo = AuthRepository()
    private val fsRepo   = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // quando clica no botao de registro
        binding.btnRegister.setOnClickListener {
            // pega os dados digitados
            val nome  = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val pass  = binding.etPass.text.toString()

            // inicia uma corrotina no escopo da activity
            lifecycleScope.launch {
                // tenta registrar o usuario
                authRepo.register(email, pass)
                    .onSuccess { user ->
                        // se der bom, salva perfil no firestore
                        fsRepo.createUserProfile(user, nome)
                        // e vai direto pra tela principal
                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                        finish()
                    }
                    // se der ruim, mostra o erro
                    .onFailure { show(it) }
            }
        }

        // se clicar no texto pra ir pro login
        binding.tvToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // funcaozinha pra mostrar erro com toast
    private fun show(t: Throwable) =
        Toast.makeText(this, t.message ?: "erro", Toast.LENGTH_SHORT).show()
}
