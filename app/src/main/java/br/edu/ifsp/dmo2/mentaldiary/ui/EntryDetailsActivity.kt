package br.edu.ifsp.dmo2.mentaldiary.ui

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.mentaldiary.databinding.ActivityEntryDetailsBinding
import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class EntryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryDetailsBinding
    private lateinit var entry: DiaryEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        entry = intent.getParcelableExtra("entry") ?: run {
            Toast.makeText(this, "Erro ao carregar entrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        carregarDados()

        binding.btnEditar.setOnClickListener {
            val intent = Intent(this, EntryFormActivity::class.java)
            intent.putExtra("entry", entry)
            startActivityForResult(intent, 200)
        }

        binding.btnExcluir.setOnClickListener {
            confirmarExclusao()
        }
    }

    private fun carregarDados() {
        binding.tvTexto.text = entry.texto
        binding.tvHumor.text = "Humor: ${entry.humor}"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataFormatada = entry.dataCriacao?.toDate()?.let { sdf.format(it) } ?: ""
        binding.tvData.text = "Data: $dataFormatada"

        if (!entry.imagemUrl.isNullOrEmpty()) {
            binding.ivImagemPreview.visibility = android.view.View.VISIBLE
            Glide.with(this).load(Uri.parse(entry.imagemUrl)).into(binding.ivImagemPreview)
        } else {
            binding.ivImagemPreview.visibility = android.view.View.GONE
        }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Entrada")
            .setMessage("Tem certeza que deseja excluir esta entrada?")
            .setPositiveButton("Sim") { _, _ -> excluirEntrada() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirEntrada() {
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        firestore.collection("entries")
            .document(entry.id)
            .delete()
            .addOnSuccessListener {
                entry.imagemUrl?.let { url ->
                    val storageRef = storage.getReferenceFromUrl(url)
                    storageRef.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Entrada e imagem excluídas com sucesso!", Toast.LENGTH_SHORT).show()
                            reiniciarHomeActivity()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Entrada excluída, mas falha ao remover imagem.", Toast.LENGTH_SHORT).show()
                            reiniciarHomeActivity()
                        }
                } ?: run {
                    Toast.makeText(this, "Entrada excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    reiniciarHomeActivity()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao excluir entrada", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reiniciarHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK // Limpa a pilha de atividades e reinicia HomeActivity
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == RESULT_OK) {
            reiniciarHomeActivity()
        }
    }
}
