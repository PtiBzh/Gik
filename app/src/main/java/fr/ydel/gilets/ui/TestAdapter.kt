package fr.ydel.gilets.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.ydel.gilets.R
import fr.ydel.gilets.data.TestResult
import java.time.format.DateTimeFormatter

class TestAdapter(private val onDeleteClick: (TestResult) -> Unit) : 
    ListAdapter<TestResult, TestAdapter.TestViewHolder>(TestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test, parent, false)
        return TestViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        val test = getItem(position)
        holder.bind(test)
        holder.deleteButton.setOnClickListener { onDeleteClick(test) }
    }

    class TestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
        private val modeleText: TextView = view.findViewById(R.id.giletModele)
        private val idText: TextView = view.findViewById(R.id.giletId)
        private val constatText: TextView = view.findViewById(R.id.constat)
        private val timestampText: TextView = view.findViewById(R.id.timestamp)

        fun bind(test: TestResult) {
            modeleText.text = test.giletModele
            idText.text = test.giletIdClub
            constatText.text = test.constat
            
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            timestampText.text = test.timestamp.format(formatter)
            
            val colorRes = when {
                test.estConforme == true -> R.color.verdictConforme
                test.constat.contains("visuel") -> R.color.verdictWarning
                else -> R.color.verdictNonConforme
            }
            constatText.setTextColor(view.context.getColor(colorRes))
        }
    }

    class TestDiffCallback : DiffUtil.ItemCallback<TestResult>() {
        override fun areItemsTheSame(oldItem: TestResult, newItem: TestResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TestResult, newItem: TestResult): Boolean {
            return oldItem == newItem
        }
    }
}
