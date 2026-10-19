package fr.ydel.gilets

import fr.ydel.gilets.data.MeasurementMode
import fr.ydel.gilets.data.TestResult
import fr.ydel.gilets.util.CsvExporter
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

class CsvExporterTest {

    @Test
    fun testExportBasicCsv() {
        val test = TestResult(
            id = 1,
            giletIdCanonique = "FAB001",
            giletIdClub = "23-001",
            giletModele = "Gilet Adulte",
            flottabiliteNominale = 200.0,
            lectureKg = 20.4,
            flottabiliteEvaluee = 199.92,
            delta = -0.08,
            constat = "Conforme",
            timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0),
            estConforme = true,
            modeMesure = MeasurementMode.BANC_POULIE,
            lestKg = null
        )

        val outputStream = ByteArrayOutputStream()
        CsvExporter.exportTestsToCsv(listOf(test), outputStream)
        
        val csvContent = outputStream.toString(Charsets.UTF_8)
        
        assertTrue(csvContent.startsWith("\uFEFF"))
        assertTrue(csvContent.contains("Modèle;ID canonique;ID club;Test (Visu ou ×100g);Lest (×100g);Flotta (N);Flotta évaluée N;Δ (N);Constat;Horodatage"))
        assertTrue(csvContent.contains("Gilet Adulte;FAB001;23-001"))
        assertTrue(csvContent.contains("2040"))
        assertTrue(csvContent.contains("200,0"))
        assertTrue(csvContent.contains("Conforme"))
        assertTrue(csvContent.contains("2024-01-15 10:30:00"))
    }

    @Test
    fun testExportAncienProtocoleCsv() {
        val test = TestResult(
            id = 1,
            giletIdCanonique = "FAB001",
            giletIdClub = "23-001",
            giletModele = "Gilet Adulte",
            flottabiliteNominale = 200.0,
            lectureKg = 9.0,
            flottabiliteEvaluee = 19.6,
            delta = -180.4,
            constat = "Non conforme (flottabilité)",
            timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0),
            estConforme = false,
            modeMesure = MeasurementMode.ANCIEN_PROTOCOLE,
            lestKg = 11.0
        )

        val outputStream = ByteArrayOutputStream()
        CsvExporter.exportTestsToCsv(listOf(test), outputStream)
        
        val csvContent = outputStream.toString(Charsets.UTF_8)
        
        assertTrue(csvContent.contains("110"))
        assertTrue(csvContent.contains("900"))
        assertTrue(csvContent.contains("Non conforme (flottabilité)"))
    }

    @Test
    fun testExportSansLectureCsv() {
        val test = TestResult(
            id = 1,
            giletIdCanonique = "FAB001",
            giletIdClub = "23-001",
            giletModele = "Gilet Adulte",
            flottabiliteNominale = 200.0,
            lectureKg = null,
            flottabiliteEvaluee = null,
            delta = null,
            constat = "Non conforme (visuel)",
            timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0),
            estConforme = false,
            modeMesure = MeasurementMode.BANC_POULIE,
            lestKg = null
        )

        val outputStream = ByteArrayOutputStream()
        CsvExporter.exportTestsToCsv(listOf(test), outputStream)
        
        val csvContent = outputStream.toString(Charsets.UTF_8)
        
        assertTrue(csvContent.contains(";-"))
        assertTrue(csvContent.contains("Non conforme (visuel)"))
    }

    @Test
    fun testExportMultipleTests() {
        val test1 = TestResult(
            id = 1,
            giletIdCanonique = "FAB001",
            giletIdClub = "23-001",
            giletModele = "Gilet Adulte",
            flottabiliteNominale = 200.0,
            lectureKg = 20.4,
            flottabiliteEvaluee = 199.92,
            delta = -0.08,
            constat = "Conforme",
            timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0),
            estConforme = true,
            modeMesure = MeasurementMode.BANC_POULIE,
            lestKg = null
        )

        val test2 = TestResult(
            id = 2,
            giletIdCanonique = "FAB002",
            giletIdClub = "23-002",
            giletModele = "Gilet Adulte",
            flottabiliteNominale = 200.0,
            lectureKg = null,
            flottabiliteEvaluee = null,
            delta = null,
            constat = "Non conforme (visuel)",
            timestamp = LocalDateTime.of(2024, 1, 15, 10, 31, 0),
            estConforme = false,
            modeMesure = MeasurementMode.BANC_POULIE,
            lestKg = null
        )

        val outputStream = ByteArrayOutputStream()
        CsvExporter.exportTestsToCsv(listOf(test1, test2), outputStream)
        
        val csvContent = outputStream.toString(Charsets.UTF_8)
        
        assertTrue(csvContent.contains("23-001"))
        assertTrue(csvContent.contains("23-002"))
        assertTrue(csvContent.contains("Conforme"))
        assertTrue(csvContent.contains("Non conforme (visuel)"))
    }
}
