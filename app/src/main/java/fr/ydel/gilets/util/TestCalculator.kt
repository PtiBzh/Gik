package fr.ydel.gilets.util

import fr.ydel.gilets.data.MeasurementMode
import fr.ydel.gilets.data.TestResult
import java.time.LocalDateTime

object TestCalculator {
    private const val GRAVITY = 9.8

    fun calculateTestResult(
        gilet: fr.ydel.gilets.data.Gilet?,
        lectureKg: Double?,
        modeMesure: MeasurementMode,
        lestKg: Double? = null,
        manualSeuil: Double? = null
    ): TestResult {
        val giletIdCanonique = gilet?.idCanonique ?: "HORS_LISTE"
        val giletIdClub = gilet?.idClub ?: "HORS_LISTE"
        val giletModele = gilet?.modele ?: "Inconnu"
        val flottabiliteNominale = manualSeuil ?: gilet?.flottabiliteNominale ?: 0.0

        val flottabiliteEvaluee = when {
            lectureKg == null -> null
            modeMesure == MeasurementMode.BANC_POULIE -> lectureKg * GRAVITY
            modeMesure == MeasurementMode.ANCIEN_PROTOCOLE -> (lestKg ?: 11.0 - lectureKg) * GRAVITY
        }

        val delta = flottabiliteEvaluee?.let { it - flottabiliteNominale }
        val estConforme = flottabiliteEvaluee?.let { it >= flottabiliteNominale }

        val constat = when {
            lectureKg == null -> {
                when {
                    flottabiliteNominale == 0.0 -> "À vérifier (seuil inconnu)"
                    else -> "Non conforme (visuel)"
                }
            }
            estConforme == true -> "Conforme"
            else -> "Non conforme (flottabilité)"
        }

        return TestResult(
            giletIdCanonique = giletIdCanonique,
            giletIdClub = giletIdClub,
            giletModele = giletModele,
            flottabiliteNominale = flottabiliteNominale,
            lectureKg = lectureKg,
            flottabiliteEvaluee = flottabiliteEvaluee?.let { "%.1f".format(it).toDouble() },
            delta = delta?.let { "%.1f".format(it).toDouble() },
            constat = constat,
            timestamp = LocalDateTime.now(),
            estConforme = estConforme,
            modeMesure = modeMesure,
            lestKg = if (modeMesure == MeasurementMode.ANCIEN_PROTOCOLE) lestKg else null
        )
    }

    fun createManualTestResult(
        giletId: String,
        constat: String,
        modeMesure: MeasurementMode,
        lestKg: Double? = null
    ): TestResult {
        return TestResult(
            giletIdCanonique = giletId,
            giletIdClub = giletId,
            giletModele = "Inconnu",
            flottabiliteNominale = 0.0,
            lectureKg = null,
            flottabiliteEvaluee = null,
            delta = null,
            constat = constat,
            timestamp = LocalDateTime.now(),
            estConforme = false,
            modeMesure = modeMesure,
            lestKg = if (modeMesure == MeasurementMode.ANCIEN_PROTOCOLE) lestKg else null
        )
    }
}
