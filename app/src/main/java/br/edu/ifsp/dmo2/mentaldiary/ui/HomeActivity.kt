package br.edu.ifsp.dmo2.mentaldiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo2.mentaldiary.R
import br.edu.ifsp.dmo2.mentaldiary.auth.LoginActivity
import br.edu.ifsp.dmo2.mentaldiary.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.mentaldiary.firebase.AuthRepository
import br.edu.ifsp.dmo2.mentaldiary.firebase.FirestoreRepository
import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import br.edu.ifsp.dmo2.mentaldiary.ui.adapter.EntryAdapter
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val authRepo = AuthRepository()
    private val fsRepo   = FirestoreRepository()
    private val adapter  = EntryAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)

        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, EntryFormActivity::class.java))   // Stub por enquanto
        }

        fetchEntries()
    }
    
    private fun fetchEntries() {
        val uid = authRepo.currentUser()?.uid ?: return
        fsRepo.getEntriesForUser(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DiaryEntry::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                adapter.update(list)
            }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            authRepo.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
