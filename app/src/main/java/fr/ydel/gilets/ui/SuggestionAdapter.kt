package fr.ydel.gilets.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.ydel.gilets.R
import fr.ydel.gilets.data.Gilet

class SuggestionAdapter(private val onClick: (Gilet) -> Unit) : 
    ListAdapter<Gilet, SuggestionAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val gilet = getItem(position)
        holder.bind(gilet)
        holder.itemView.setOnClickListener { onClick(gilet) }
    }

    class SuggestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val modeleText: TextView = view.findViewById(R.id.giletModele)
        private val infoText: TextView = view.findViewById(R.id.giletInfo)

        fun bind(gilet: Gilet) {
            modeleText.text = gilet.modele
            infoText.text = "${gilet.idClub} - Seuil: ${gilet.flottabiliteNominale} N"
        }
    }

    class SuggestionDiffCallback : DiffUtil.ItemCallback<Gilet>() {
        override fun areItemsTheSame(oldItem: Gilet, newItem: Gilet): Boolean {
            return oldItem.idCanonique == newItem.idCanonique
        }

        override fun areContentsTheSame(oldItem: Gilet, newItem: Gilet): Boolean {
            return oldItem == newItem
        }
    }
}
