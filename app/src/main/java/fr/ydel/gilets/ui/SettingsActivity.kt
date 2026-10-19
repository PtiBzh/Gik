package fr.ydel.gilets.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.ydel.gilets.R
import fr.ydel.gilets.data.MeasurementMode
import fr.ydel.gilets.databinding.ActivitySettingsBinding
import fr.ydel.gilets.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importCampaign(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        // Measurement mode radio buttons
        binding.measurementModeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.bancPoulieRadio -> MeasurementMode.BANC_POULIE
                R.id.ancienProtocoleRadio -> MeasurementMode.ANCIEN_PROTOCOLE
                else -> MeasurementMode.BANC_POULIE
            }
            viewModel.setMeasurementMode(mode)
        }

        // Set initial radio button selection
        val currentMode = viewModel.measurementMode.value ?: MeasurementMode.BANC_POULIE
        val radioId = when (currentMode) {
            MeasurementMode.BANC_POULIE -> R.id.bancPoulieRadio
            MeasurementMode.ANCIEN_PROTOCOLE -> R.id.ancienProtocoleRadio
        }
        binding.measurementModeGroup.check(radioId)

        // Lest value input
        binding.lestInput.setText(viewModel.lestValue.value.toString())
        binding.lestInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = binding.lestInput.text.toString()
                val value = text.toDoubleOrNull() ?: 11.0
                viewModel.setLestValue(value)
            }
        }

        // Import campaign button
        binding.importCampaignButton.setOnClickListener {
            importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values"))
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.lestValue.observe(this) { value ->
            if (value != binding.lestInput.text.toString().toDoubleOrNull()) {
                binding.lestInput.setText(value.toString())
            }
        }

        viewModel.importResult.observe(this) { result ->
            result?.onSuccess { count ->
                Toast.makeText(this, "$count gilets importés", Toast.LENGTH_SHORT).show()
                viewModel.resetImportResult()
            }?.onFailure { exception ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Erreur")
                    .setMessage("Erreur lors de l'import: ${exception.message}")
                    .setPositiveButton("OK", null)
                    .show()
                viewModel.resetImportResult()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.importCampaignButton.isEnabled = !isLoading
        }
    }
}
