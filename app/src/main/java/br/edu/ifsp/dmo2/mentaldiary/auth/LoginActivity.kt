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
    private lateinit var binding: ActivityLoginBinding
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass  = binding.etPass.text.toString()
            lifecycleScope.launch {
                authRepo.login(email, pass)
                    .onSuccess {
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                    .onFailure { show(it) }
            }
        }

        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    private fun show(t: Throwable) =
        Toast.makeText(this, t.message ?: "Erro", Toast.LENGTH_SHORT).show()
}
