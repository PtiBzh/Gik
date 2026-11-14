package fr.ydel.gilets.data

import java.io.BufferedReader

/** Une ligne de la feuille de campagne (une ligne = un gilet). */
data class Gilet(
    val modele: String,
    val idCanonique: String,   // ex. 240200009
    val idClub: String,        // ex. 55-25 (peut être vide)
    val lestHg: Int,           // colonne « Lest (×100g) », ex. 110
    val flottaNominaleN: Int,  // colonne « Flotta (N) », ex. 50
)

/** Un résultat de test saisi dans l'app. */
data class Resultat(
    val idCanonique: String,
    val idClub: String,
    val modele: String,
    val mode: String,            // "poulie" ou "ancien"
    val lestHg: Int,             // recopié de la feuille (colonne Lest)
    val lecture: Double?,        // kg lus au peson (null si HS sans lecture)
    val hsVisuel: Boolean,       // HS sans lecture (coule / flotte selon mode)
    val flottaEvalueeN: Double?, // calculée
    val deltaN: Double?,         // flotta évaluée − nominale
    val verdict: String,         // "OK" / "HS"
    val horodatage: String,      // ISO-8601 local
)

object Protocole {
    const val MODE_POULIE = "poulie"   // montage poulie : lecture = flottabilité directe
    const val MODE_ANCIEN = "ancien"   // ancien protocole : (Lest − Test) × 9,8
    const val G = 9.8

    /**
     * Calcule la flottabilité (N) selon le mode.
     * @param lectureKg valeur lue au peson, en kg
     * @param lestHg lest de la ligne (colonne ×100g), utilisé en mode ancien
     */
    fun flottabiliteN(mode: String, lectureKg: Double, lestHg: Int): Double = when (mode) {
        MODE_POULIE -> lectureKg * G
        // Test (×100g) = lecture en hectogrammes : (Lest − Test)/10 kg → ×9,8
        MODE_ANCIEN -> (lestHg / 10.0 - lectureKg) * G
        else -> throw IllegalArgumentException("mode inconnu: $mode")
    }

    /** Verdict : conforme si la flottabilité évaluée atteint la nominale. */
    fun verdict(flottaEvalueeN: Double, flottaNominaleN: Int): String =
        if (flottaEvalueeN >= flottaNominaleN) "OK" else "HS"

    /** Libellé du bouton HS sans lecture, selon le mode. */
    fun libelleHs(mode: String): String =
        if (mode == MODE_POULIE) "HS — coule" else "HS — flotte"
}

/** Parsing du CSV de campagne : séparateur « ; », UTF-8 avec BOM. */
object Csv {
    /** En-tête attendu de la feuille de campagne. */
    val ENTETE = listOf(
        "Modèle", "ID canonique", "ID club", "Test (Visu ou ×100g)",
        "Lest (×100g)", "Flotta (N)", "Flotta évaluée N", "Δ (N)", "Constat"
    )

    fun parse(text: String): List<Gilet> {
        val lignes = text.replace("\uFEFF", "").lines().filter { it.isNotBlank() }
        if (lignes.isEmpty()) return emptyList()
        val sep = if (lignes[0].count { it == ';' } >= 4) ';' else ','
        return lignes.drop(1).mapNotNull { ligne ->
            val c = ligne.split(sep)
            if (c.size < 6) return@mapNotNull null
            val lest = c[4].trim().toIntOrNull() ?: 110
            val flotta = c[5].trim().toIntOrNull() ?: 0
            Gilet(
                modele = c[0].trim(),
                idCanonique = c[1].trim(),
                idClub = c[2].trim(),
                lestHg = lest,
                flottaNominaleN = flotta,
            )
        }
    }

    /**
     * Recherche d'un gilet depuis une saisie ou un scan.
     * Règle (cahier des charges) : n° de série exact, OU contenu dans la
     * valeur scannée ; l'ID club (ex. 55-25) matche aussi.
     */
    fun cherche(gilets: List<Gilet>, q: String): Gilet? {
        val s = q.trim()
        if (s.isEmpty()) return null
        gilets.firstOrNull { it.idCanonique == s || it.idClub == s }?.let { return it }
        gilets.firstOrNull { it.idCanonique.isNotEmpty() && s.contains(it.idCanonique) }?.let { return it }
        gilets.firstOrNull { it.idClub.isNotEmpty() && s.contains(it.idClub) }?.let { return it }
        // Saisie partielle : préfixe sur ID canonique ou club.
        return gilets.firstOrNull { it.idCanonique.startsWith(s) || it.idClub.startsWith(s) }
    }
}


