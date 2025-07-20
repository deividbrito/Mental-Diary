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

        // pega a entrada passada por intent
        entry = intent.getParcelableExtra("entry") ?: run {
            Toast.makeText(this, "erro ao carregar entrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // mostra os dados da entrada na tela
        carregarDados()

        // botao pra editar a entrada
        binding.btnEditar.setOnClickListener {
            val intent = Intent(this, EntryFormActivity::class.java)
            intent.putExtra("entry", entry)
            startActivityForResult(intent, 200)
        }

        // botao pra excluir a entrada
        binding.btnExcluir.setOnClickListener {
            confirmarExclusao()
        }
    }

    // mostra texto, data, humor e imagem
    private fun carregarDados() {
        binding.tvTexto.text = entry.texto
        binding.tvHumor.text = "humor: ${entry.humor}"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dataFormatada = entry.dataCriacao?.toDate()?.let { sdf.format(it) } ?: ""
        binding.tvData.text = "data: $dataFormatada"

        if (!entry.imagemUrl.isNullOrEmpty()) {
            binding.ivImagemPreview.visibility = android.view.View.VISIBLE
            Glide.with(this).load(Uri.parse(entry.imagemUrl)).into(binding.ivImagemPreview)
        } else {
            binding.ivImagemPreview.visibility = android.view.View.GONE
        }
    }

    // abre dialogo pra confirmar exclusao
    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("excluir entrada")
            .setMessage("tem certeza que deseja excluir esta entrada?")
            .setPositiveButton("sim") { _, _ -> excluirEntrada() }
            .setNegativeButton("cancelar", null)
            .show()
    }

    // remove entrada do firestore (e a imagem do storage se tiver)
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
                            Toast.makeText(this, "entrada e imagem excluidas com sucesso!", Toast.LENGTH_SHORT).show()
                            reiniciarHomeActivity()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "entrada excluida, mas falha ao remover imagem", Toast.LENGTH_SHORT).show()
                            reiniciarHomeActivity()
                        }
                } ?: run {
                    Toast.makeText(this, "entrada excluida com sucesso!", Toast.LENGTH_SHORT).show()
                    reiniciarHomeActivity()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "erro ao excluir entrada", Toast.LENGTH_SHORT).show()
            }
    }

    // reinicia a home depois de editar ou excluir
    private fun reiniciarHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // se voltar da tela de edicao, recarrega a home
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == RESULT_OK) {
            reiniciarHomeActivity()
        }
    }
}
