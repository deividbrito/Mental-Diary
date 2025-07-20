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
    // binding da tela
    private lateinit var binding: ActivityHomeBinding

    // instancias dos repos de auth e firestore
    private val authRepo = AuthRepository()
    private val fsRepo = FirestoreRepository()

    // adapter do recycler pra mostrar as entradas
    private lateinit var adapter: EntryAdapter

    // lista com todas as entradas do usuario
    private var allEntries: List<DiaryEntry> = emptyList()

    // opcoes escolhidas nos filtros
    private var selectedHumor: String = "Todos"
    private var selectedOrder: String = "Mais recentes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar da activity
        setSupportActionBar(binding.toolbar)

        // configura o adapter com clique pra ver detalhes
        adapter = EntryAdapter(mutableListOf()) { entry ->
            val intent = Intent(this, EntryDetailsActivity::class.java)
            intent.putExtra("entry", entry)
            startActivity(intent)
        }

        // seta o layout da lista
        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = adapter

        // acao do botao de adicionar nova entrada
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, EntryFormActivity::class.java)
            startActivityForResult(intent, 100)
        }

        // configura os spinners de filtro
        setupSpinners()

        // busca entradas do usuario no firestore
        fetchEntries()
    }

    // se voltar da tela de adicionar entrada, recarrega a lista
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            fetchEntries()
        }
    }

    // funcao que prepara os spinners de filtro
    private fun setupSpinners() {
        val humorOptions = listOf("Todos", "Feliz", "Triste", "Neutro")
        val dataOptions = listOf("Mais recentes", "Mais antigos")

        // spinner de humor
        binding.spinnerHumor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, humorOptions)
        // spinner de data
        binding.spinnerData.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dataOptions)

        // escuta quando o usuario muda o filtro de humor
        binding.spinnerHumor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedHumor = humorOptions[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // escuta quando muda o filtro de ordenacao
        binding.spinnerData.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedOrder = dataOptions[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // busca todas as entradas do usuario no firestore
    private fun fetchEntries() {
        val uid = authRepo.currentUser()?.uid ?: return
        fsRepo.getEntriesForUser(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                // transforma os documentos em objetos DiaryEntry
                allEntries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DiaryEntry::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // aplica filtros se houver
                applyFilters()
            }
    }

    // aplica os filtros escolhidos pelo usuario na lista
    private fun applyFilters() {
        var filtered = allEntries

        // se tiver filtro de humor, aplica
        if (selectedHumor != "Todos") {
            filtered = filtered.filter { it.humor == selectedHumor }
        }

        // ordena conforme o tipo escolhido
        filtered = when (selectedOrder) {
            "Mais recentes" -> filtered.sortedByDescending { it.dataCriacao?.toDate() }
            "Mais antigos" -> filtered.sortedBy { it.dataCriacao?.toDate() }
            else -> filtered
        }

        // atualiza o adapter com a lista filtrada
        adapter.update(filtered)
    }

    // inflar o menu (aquele de sair no canto da toolbar)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    // quando clica em "sair"
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
