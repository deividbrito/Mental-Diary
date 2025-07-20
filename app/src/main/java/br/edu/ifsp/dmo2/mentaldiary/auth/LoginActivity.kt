package br.edu.ifsp.dmo2.mentaldiary.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.mentaldiary.databinding.ActivityLoginBinding
import br.edu.ifsp.dmo2.mentaldiary.firebase.AuthRepository
import br.edu.ifsp.dmo2.mentaldiary.ui.HomeActivity
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    // binding da tela de login
    private lateinit var binding: ActivityLoginBinding
    // repositorio de autenticacao
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // quando clica no botao de login
        binding.btnLogin.setOnClickListener {
            // pega o que o usuario digitou
            val email = binding.etEmail.text.toString()
            val pass  = binding.etPass.text.toString()

            // inicia corrotina pra logar
            lifecycleScope.launch {
                authRepo.login(email, pass)
                    .onSuccess {
                        // se logar certo, vai pra home
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                    // se der erro, mostra mensagem
                    .onFailure { show(it) }
            }
        }

        // se clicar pra ir pro cadastro
        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // funcao pra mostrar erro com toast
    private fun show(t: Throwable) =
        Toast.makeText(this, t.message ?: "erro", Toast.LENGTH_SHORT).show()
}
