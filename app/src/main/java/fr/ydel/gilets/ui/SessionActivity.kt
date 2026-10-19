package fr.ydel.gilets.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.ydel.gilets.databinding.ActivitySessionBinding
import fr.ydel.gilets.util.CsvExporter
import fr.ydel.gilets.viewmodel.SessionViewModel
import java.time.format.DateTimeFormatter

class SessionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySessionBinding
    private lateinit var viewModel: SessionViewModel
    private lateinit var testsAdapter: TestAdapter

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { exportToCsv(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(SessionViewModel::class.java)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        testsAdapter = TestAdapter { testResult ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous vraiment supprimer ce test ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.deleteTest(testResult.id)
                }
                .setNegativeButton("Non", null)
                .show()
        }

        binding.testsRecycler.apply {
            layoutManager = LinearLayoutManager(this@SessionActivity)
            adapter = testsAdapter
        }

        binding.exportButton.setOnClickListener {
            val timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val filename = "gilets-export-$timestamp.csv"
            exportLauncher.launch(filename)
        }

        binding.deleteAllButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Confirmer")
                .setMessage("Voulez-vous vraiment effacer tous les tests ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.deleteAllTests()
                }
                .setNegativeButton("Non", null)
                .show()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.tests.observe(this) { tests ->
            testsAdapter.submitList(tests)
        }

        viewModel.stats.observe(this) { stats ->
            binding.testesCount.text = stats.total.toString()
            binding.conformesCount.text = stats.conformes.toString()
            binding.hsCount.text = stats.nonConformes.toString()
            binding.horsListeCount.text = stats.horsListe.toString()
        }
    }

    private fun exportToCsv(uri: Uri) {
        val tests = viewModel.tests.value ?: emptyList()
        
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            CsvExporter.exportTestsToCsv(tests, outputStream)
            Toast.makeText(this, "Export CSV terminé", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "Erreur lors de l'export", Toast.LENGTH_SHORT).show()
        }
    }
}
