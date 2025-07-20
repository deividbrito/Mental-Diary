package br.edu.ifsp.dmo2.mentaldiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.mentaldiary.auth.LoginActivity
import br.edu.ifsp.dmo2.mentaldiary.firebase.AuthRepository
import br.edu.ifsp.dmo2.mentaldiary.ui.HomeActivity

class MainActivity : AppCompatActivity() {
    // repositorio de auth pra saber se ta logado
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // se nao tiver usuario logado, vai pro login
        // se tiver, vai direto pra home
        val next = if (authRepo.currentUser() == null)
            Intent(this, LoginActivity::class.java)
        else
            Intent(this, HomeActivity::class.java)

        startActivity(next)
        finish()
    }
}

