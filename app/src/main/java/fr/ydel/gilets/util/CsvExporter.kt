package fr.ydel.gilets.util

import fr.ydel.gilets.data.MeasurementMode
import fr.ydel.gilets.data.TestResult
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter

object CsvExporter {
    private const val SEPARATOR = ";"
    private const val BOM = "\uFEFF"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun exportTestsToCsv(tests: List<TestResult>, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
        
        writer.write(BOM)
        writer.write("Modèle;ID canonique;ID club;Test (Visu ou ×100g);Lest (×100g);Flotta (N);Flotta évaluée N;Δ (N);Constat;Horodatage\n")

        tests.forEach { test ->
            val testValue = test.lectureKg?.let { "%.0f".format(it * 100) } ?: "-"
            val lestValue = if (test.modeMesure == MeasurementMode.ANCIEN_PROTOCOLE) "110" else ""
            val flottabiliteEvaluee = test.flottabiliteEvaluee?.let { "%.1f".format(it).replace(".", ",") } ?: ""
            val delta = test.delta?.let { "%.1f".format(it).replace(".", ",") } ?: ""
            val flottabiliteNominale = "%.1f".format(test.flottabiliteNominale).replace(".", ",")
            val timestamp = test.timestamp.format(dateFormatter)

            writer.write("${test.giletModele};${test.giletIdCanonique};${test.giletIdClub};$testValue;$lestValue;$flottabiliteNominale;$flottabiliteEvaluee;$delta;${test.constat};$timestamp\n")
        }

        writer.flush()
        writer.close()
    }
}
