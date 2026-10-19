package fr.ydel.gilets

import fr.ydel.gilets.data.Gilet
import fr.ydel.gilets.data.MeasurementMode
import fr.ydel.gilets.util.TestCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TestCalculatorTest {

    @Test
    fun testBancPoulieConforme() {
        val gilet = Gilet(
            idCanonique = "FAB001",
            modele = "Gilet Adulte",
            idClub = "23-001",
            flottabiliteNominale = 200.0
        )

        val result = TestCalculator.calculateTestResult(
            gilet = gilet,
            lectureKg = 20.4,
            modeMesure = MeasurementMode.BANC_POULIE,
            lestKg = null
        )

        assertEquals(199.92, result.flottabiliteEvaluee, 0.01)
        assertEquals(-0.08, result.delta, 0.01)
        assertEquals(false, result.estConforme)
        assertEquals("Non conforme (flottabilité)", result.constat)
    }

    @Test
    fun testBancPoulieConformeExact() {
        val gilet = Gilet(
            idCanonique = "FAB001",
            modele = "Gilet Adulte",
            idClub = "23-001",
            flottabiliteNominale = 200.0
        )

        val result = TestCalculator.calculateTestResult(
            gilet = gilet,
            lectureKg = 20.40816326530612,
            modeMesure = MeasurementMode.BANC_POULIE,
            lestKg = null
        )

        assertEquals(200.0, result.flottabiliteEvaluee, 0.01)
        assertEquals(0.0, result.delta, 0.01)
        assertEquals(true, result.estConforme)
        assertEquals("Conforme", result.constat)
    }

    @Test
    fun testAncienProtocoleConforme() {
        val gilet = Gilet(
            idCanonique = "FAB006",
            modele = "Gilet Enfant",
            idClub = "23-006",
            flottabiliteNominale = 15.0
        )

        val result = TestCalculator.calculateTestResult(
            gilet = gilet,
            lectureKg = 10.0,
            modeMesure = MeasurementMode.ANCIEN_PROTOCOLE,
            lestKg = 11.0
        )

        assertEquals(9.8, result.flottabiliteEvaluee, 0.01)
        assertEquals(-5.2, result.delta, 0.01)
        assertEquals(false, result.estConforme)
        assertEquals("Non conforme (flottabilité)", result.constat)
    }

    @Test
    fun testSansLectureVisuelHS() {
        val result = TestCalculator.createManualTestResult(
            giletId = "HORS_LISTE",
            constat = "Non conforme (visuel)",
            modeMesure = MeasurementMode.BANC_POULIE
        )

        assertNull(result.lectureKg)
        assertNull(result.flottabiliteEvaluee)
        assertNull(result.delta)
        assertEquals(false, result.estConforme)
        assertEquals("Non conforme (visuel)", result.constat)
    }

    @Test
    fun testHorsListeAVerifier() {
        val result = TestCalculator.calculateTestResult(
            gilet = null,
            lectureKg = 20.0,
            modeMesure = MeasurementMode.BANC_POULIE,
            manualSeuil = null
        )

        assertEquals("À vérifier (seuil inconnu)", result.constat)
        assertEquals(false, result.estConforme)
    }

    @Test
    fun testHorsListeAvecSeuil() {
        val result = TestCalculator.calculateTestResult(
            gilet = null,
            lectureKg = 20.0,
            modeMesure = MeasurementMode.BANC_POULIE,
            manualSeuil = 150.0
        )

        assertEquals(196.0, result.flottabiliteEvaluee, 0.01)
        assertEquals(46.0, result.delta, 0.01)
        assertEquals(true, result.estConforme)
        assertEquals("Conforme", result.constat)
    }

    @Test
    fun testCouleHS() {
        val result = TestCalculator.createManualTestResult(
            giletId = "FAB001",
            constat = "Non conforme (banc poulie)",
            modeMesure = MeasurementMode.BANC_POULIE
        )

        assertEquals("Non conforme (banc poulie)", result.constat)
        assertEquals(false, result.estConforme)
    }

    @Test
    fun testFlotteHS() {
        val result = TestCalculator.createManualTestResult(
            giletId = "FAB001",
            constat = "Non conforme (ancien protocole)",
            modeMesure = MeasurementMode.ANCIEN_PROTOCOLE
        )

        assertEquals("Non conforme (ancien protocole)", result.constat)
        assertEquals(false, result.estConforme)
    }

    @Test
    fun testIdNormalizer() {
        val normalizer = fr.ydel.gilets.util.IdNormalizer
        
        assertEquals("FAB001", normalizer.normalizeId("FAB001"))
        assertEquals("FAB001", normalizer.normalizeId("fab001"))
        assertEquals("FAB001", normalizer.normalizeId("Fab 001"))
        assertEquals("FAB001", normalizer.normalizeId("Fab#001"))
        assertEquals("FAB001", normalizer.normalizeId("Fab #001"))
    }

    @Test
    fun testIdExtractionFromBarcode() {
        val normalizer = fr.ydel.gilets.util.IdNormalizer
        
        assertEquals("FAB001", normalizer.extractIdFromBarcode("FAB001"))
        assertEquals("FAB001", normalizer.extractIdFromBarcode("prefix-FAB001-suffix"))
        assertEquals("FAB001", normalizer.extractIdFromBarcode("ABC123FAB001XYZ"))
        assertEquals("23001", normalizer.extractIdFromBarcode("23-001"))
        assertEquals("23001", normalizer.extractIdFromBarcode("prefix23-001suffix"))
    }
}
