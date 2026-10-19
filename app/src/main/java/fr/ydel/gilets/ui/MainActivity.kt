package fr.ydel.gilets.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import fr.ydel.gilets.R
import fr.ydel.gilets.data.MeasurementMode
import fr.ydel.gilets.databinding.ActivityMainBinding
import fr.ydel.gilets.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var suggestionsAdapter: SuggestionAdapter

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.onBarcodeScanned(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupViews()
        setupObservers()
        
        // Load measurement mode and lest from settings
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val mode = prefs.getString("measurement_mode", MeasurementMode.BANC_POULIE.name)
        val lest = prefs.getFloat("lest_value", 11.0f).toDouble()
        
        viewModel.setMeasurementMode(MeasurementMode.valueOf(mode ?: MeasurementMode.BANC_POULIE.name))
        viewModel.setLestValue(lest)
    }

    private fun setupViews() {
        // Gilet ID input
        val giletInput = binding.giletInput
        giletInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onGiletIdChanged(s.toString())
            }
        })

        // Scan button
        binding.scanButton.setOnClickListener {
            launchScan()
        }

        // Lecture input
        val lectureInput = binding.lectureInput
        lectureInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onLectureChanged(s.toString())
            }
        })

        // Validate button
        binding.validateButton.setOnClickListener {
            viewModel.validateTest()
        }

        // Cancel last test button
        binding.cancelLastButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Confirmer")
                .setMessage("Voulez-vous vraiment annuler le dernier test ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.cancelLastTest()
                }
                .setNegativeButton("Non", null)
                .show()
        }

        // Session button
        binding.sessionButton.setOnClickListener {
            startActivity(Intent(this, SessionActivity::class.java))
        }

        // Settings button
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Quick action buttons
        binding.couleHsButton.setOnClickListener {
            viewModel.createHorsListeTest("Non conforme (banc poulie)")
        }

        binding.flotteHsButton.setOnClickListener {
            viewModel.createHorsListeTest("Non conforme (ancien protocole)")
        }

        binding.visuelHsButton.setOnClickListener {
            viewModel.createHorsListeTest("Non conforme (visuel)")
        }

        // Suggestions RecyclerView
        suggestionsAdapter = SuggestionAdapter { gilet ->
            viewModel.onSuggestionSelected(gilet)
        }
        binding.suggestionsRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = suggestionsAdapter
        }
    }

    private fun setupObservers() {
        // Gilet ID input
        viewModel.giletIdInput.observe(this) { input ->
            if (input != binding.giletInput.text.toString()) {
                binding.giletInput.setText(input)
            }
        }

        // Suggestions
        viewModel.suggestions.observe(this) { suggestions ->
            suggestionsAdapter.submitList(suggestions)
            binding.suggestionsRecycler.visibility = if (suggestions.isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Current gilet
        viewModel.currentGilet.observe(this) { gilet ->
            if (gilet != null) {
                binding.giletInfoContainer.visibility = View.VISIBLE
                binding.giletModele.text = gilet.modele
                binding.giletId.text = "ID: ${gilet.idClub} (${gilet.idCanonique})"
                binding.giletSeuil.text = "Seuil: ${gilet.flottabiliteNominale} N"
                binding.quickActionsContainer.visibility = View.GONE
            } else {
                binding.giletInfoContainer.visibility = View.GONE
                binding.quickActionsContainer.visibility = View.VISIBLE
            }
        }

        // Already tested
        viewModel.alreadyTested.observe(this) { alreadyTested ->
            binding.alreadyTestedWarning.visibility = if (alreadyTested) View.VISIBLE else View.GONE
        }

        // Lecture input
        viewModel.lectureInput.observe(this) { input ->
            if (input != binding.lectureInput.text.toString()) {
                binding.lectureInput.setText(input)
            }
        }

        // Test result
        viewModel.testResult.observe(this) { testResult ->
            if (testResult != null) {
                showVerdict(testResult)
            }
        }

        // Error message
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.mainContainer, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetError()
            }
        }
    }

    private fun launchScan() {
        val options = ScanOptions()
        options.setPrompt("Scanner le code-barres du gilet")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        options.captureActivity = ScanActivity::class.java
        scanLauncher.launch(options)
    }

    private fun showVerdict(testResult: fr.ydel.gilets.data.TestResult) {
        val intent = Intent(this, VerdictActivity::class.java).apply {
            putExtra("test_result", testResult)
        }
        startActivityForResult(intent, REQUEST_CODE_VERDICT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VERDICT && resultCode == RESULT_OK) {
            viewModel.saveTest()
        }
    }

    companion object {
        private const val REQUEST_CODE_VERDICT = 1001
    }
}
