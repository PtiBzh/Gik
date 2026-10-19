package fr.ydel.gilets.ui

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import fr.ydel.gilets.R
import fr.ydel.gilets.data.TestResult
import fr.ydel.gilets.databinding.ActivityVerdictBinding
import java.time.format.DateTimeFormatter

class VerdictActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerdictBinding
    private lateinit var vibrator: Vibrator
    private lateinit var testResult: TestResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerdictBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrator = vibratorManager.defaultVibrator

        testResult = intent.getSerializableExtra("test_result") as TestResult
        
        displayVerdict()
        vibrate()

        binding.saveButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun displayVerdict() {
        val verdictText = when {
            testResult.estConforme == true -> getString(R.string.verdict_conforme)
            testResult.constat.contains("visuel") -> testResult.constat
            testResult.constat.contains("à vérifier") -> testResult.constat
            else -> testResult.constat
        }

        val colorRes = when {
            testResult.estConforme == true -> R.color.verdictConforme
            else -> R.color.verdictNonConforme
        }

        binding.verdictText.text = verdictText
        binding.verdictText.setTextColor(getColor(colorRes))
        binding.verdictContainer.setBackgroundColor(getColor(colorRes))

        binding.giletInfoText.text = "${testResult.giletModele} - ${testResult.giletIdClub}"

        val details = mutableListOf<String>()
        testResult.flottabiliteEvaluee?.let { flotta ->
            details.add("Flottabilité: ${"%.1f".format(flotta)} N")
        }
        testResult.delta?.let { delta ->
            details.add("Δ: ${if (delta >= 0) "+" else ""}${"%.1f".format(delta)} N")
        }
        testResult.lectureKg?.let { lecture ->
            details.add("Lecture: ${"%.1f".format(lecture)} kg")
        }

        binding.flottabiliteText.text = details.joinToString(" | ")

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        binding.timestampText.text = testResult.timestamp.format(formatter)
    }

    private fun vibrate() {
        if (!vibrator.hasVibrator()) return

        val duration = if (testResult.estConforme == true) {
            50L // Short vibration for conforme
        } else {
            200L // Long vibration for non-conforme
        }

        vibrator.vibrate(
            VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}
