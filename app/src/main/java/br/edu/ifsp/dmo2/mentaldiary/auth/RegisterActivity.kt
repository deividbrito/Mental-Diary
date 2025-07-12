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
    private lateinit var binding: ActivityRegisterBinding
    private val authRepo = AuthRepository()
    private val fsRepo   = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val nome  = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val pass  = binding.etPass.text.toString()

            lifecycleScope.launch {
                authRepo.register(email, pass)
                    .onSuccess { user ->
                        fsRepo.createUserProfile(user, nome)
                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                        finish()
                    }
                    .onFailure { show(it) }
            }
        }

        binding.tvToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    private fun show(t: Throwable) =
        Toast.makeText(this, t.message ?: "Erro", Toast.LENGTH_SHORT).show()
}
