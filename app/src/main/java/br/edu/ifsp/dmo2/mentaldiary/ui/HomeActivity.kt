package br.edu.ifsp.dmo2.mentaldiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo2.mentaldiary.R
import br.edu.ifsp.dmo2.mentaldiary.auth.LoginActivity
import br.edu.ifsp.dmo2.mentaldiary.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.mentaldiary.firebase.AuthRepository
import br.edu.ifsp.dmo2.mentaldiary.firebase.FirestoreRepository
import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import br.edu.ifsp.dmo2.mentaldiary.ui.adapter.EntryAdapter
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val authRepo = AuthRepository()
    private val fsRepo = FirestoreRepository()
    private lateinit var adapter: EntryAdapter

    private var allEntries: List<DiaryEntry> = emptyList()
    private var selectedHumor: String = "Todos"
    private var selectedOrder: String = "Mais recentes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = EntryAdapter(mutableListOf()) { entry ->
            val intent = Intent(this, EntryDetailsActivity::class.java)
            intent.putExtra("entry", entry)
            startActivity(intent)
        }

        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, EntryFormActivity::class.java)
            startActivityForResult(intent, 100)
        }

        setupSpinners()
        fetchEntries()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            fetchEntries()
        }
    }

    private fun setupSpinners() {
        val humorOptions = listOf("Todos", "Feliz", "Triste", "Neutro")
        val dataOptions = listOf("Mais recentes", "Mais antigos")

        binding.spinnerHumor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, humorOptions)
        binding.spinnerData.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dataOptions)

        binding.spinnerHumor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedHumor = humorOptions[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerData.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedOrder = dataOptions[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchEntries() {
        val uid = authRepo.currentUser()?.uid ?: return
        fsRepo.getEntriesForUser(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                allEntries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DiaryEntry::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                applyFilters()
            }
    }

    private fun applyFilters() {
        var filtered = allEntries

        if (selectedHumor != "Todos") {
            filtered = filtered.filter { it.humor == selectedHumor }
        }

        filtered = when (selectedOrder) {
            "Mais recentes" -> filtered.sortedByDescending { it.dataCriacao?.toDate() }
            "Mais antigos" -> filtered.sortedBy { it.dataCriacao?.toDate() }
            else -> filtered
        }

        adapter.update(filtered)
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
