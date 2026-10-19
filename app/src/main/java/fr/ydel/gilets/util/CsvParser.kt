package fr.ydel.gilets.util

import fr.ydel.gilets.data.Gilet
import java.io.InputStream
import java.io.InputStreamReader

object CsvParser {
    private const val SEPARATOR = ";"
    private const val FLOATABILITE_COLUMN = 5 // 0-indexed, 6th column
    private const val ID_CANONIQUE_COLUMN = 1 // 2nd column
    private const val ID_CLUB_COLUMN = 2 // 3rd column
    private const val MODELE_COLUMN = 0 // 1st column

    fun parseCampaignCsv(inputStream: InputStream): List<Gilet> {
        val reader = InputStreamReader(inputStream, Charsets.UTF_8).buffered()
        val gilets = mutableListOf<Gilet>()
        var lineNumber = 0

        reader.useLines { lines ->
            lines.forEach { line ->
                lineNumber++
                if (line.isBlank() || line.startsWith("#")) return@forEach

                val columns = line.split(SEPARATOR)
                
                if (columns.size <= FLOATABILITE_COLUMN) {
                    throw CsvParseException("Ligne $lineNumber: Format invalide. Attendu au moins ${FLOATABILITE_COLUMN + 1} colonnes, trouvé ${columns.size}")
                }

                try {
                    val modele = columns[MODELE_COLUMN].trim()
                    val idCanonique = columns[ID_CANONIQUE_COLUMN].trim()
                    val idClub = columns[ID_CLUB_COLUMN].trim()
                    val flottabiliteNominale = columns[FLOATABILITE_COLUMN].trim().replace(',', '.').toDouble()

                    val gilet = Gilet(
                        idCanonique = idCanonique,
                        modele = modele,
                        idClub = idClub,
                        flottabiliteNominale = flottabiliteNominale,
                        test = if (columns.size > 3) columns[3].trim() else null,
                        lest = if (columns.size > 4) columns[4].trim().replace(',', '.').toDoubleOrNull() else null,
                        constat = if (columns.size > 6) columns[6].trim() else null
                    )
                    gilets.add(gilet)
                } catch (e: NumberFormatException) {
                    throw CsvParseException("Ligne $lineNumber: Valeur numérique invalide dans la colonne Flotta (N)")
                }
            }
        }

        if (gilets.isEmpty()) {
            throw CsvParseException("Aucun gilet valide trouvé dans le fichier CSV")
        }

        return gilets
    }
}

class CsvParseException(message: String) : Exception(message)
