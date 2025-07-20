package br.edu.ifsp.dmo2.mentaldiary.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.mentaldiary.R
import br.edu.ifsp.dmo2.mentaldiary.databinding.ItemEntryBinding
import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import java.text.SimpleDateFormat
import java.util.*

class EntryAdapter(
    private val items: MutableList<DiaryEntry>,
    private val onItemClick: (DiaryEntry) -> Unit
) : RecyclerView.Adapter<EntryAdapter.VH>() {

    // viewholder com binding do layout da entrada
    class VH(val binding: ItemEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    // preenche a view com os dados da entrada
    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = items[position]
        holder.binding.tvText.text = entry.texto
        holder.binding.tvDate.text = formatDate(entry.dataCriacao)
        holder.binding.ivMood.setImageResource(moodIcon(entry.humor))

        // clique no item chama funcao de clique
        holder.binding.root.setOnClickListener {
            onItemClick(entry)
        }
    }

    // atualiza a lista de entradas no adapter
    fun update(newItems: List<DiaryEntry>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // formata a data no estilo dd MMM yyyy
    private fun formatDate(ts: com.google.firebase.Timestamp?): String {
        if (ts == null) return ""
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(ts.toDate())
    }

    // retorna o icone do humor baseado na string
    private fun moodIcon(humor: String?): Int = when (humor) {
        "Feliz" -> R.drawable.ic_happy
        "Triste" -> R.drawable.ic_sad
        "Ansioso" -> R.drawable.ic_anxious
        else -> R.drawable.ic_neutral
    }
}
