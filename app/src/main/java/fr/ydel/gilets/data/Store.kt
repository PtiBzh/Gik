package fr.ydel.gilets.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Persistance locale, équivalent du localStorage de la web app.
 * - Feuille de campagne active : CSV importé (ou l'asset 2026 par défaut).
 * - Résultats : JSON dans filesDir.
 */
class Store(context: Context) {
    private val dir: File = context.filesDir
    private val fichierFeuille = File(dir, "feuille-campagne.csv")
    private val fichierResultats = File(dir, "resultats.json")

    /** Feuille active : CSV importé s'il existe, sinon l'asset embarqué. */
    fun chargerFeuilleDefaut(context: Context): String =
        if (fichierFeuille.exists()) fichierFeuille.readText()
        else context.assets.open("campagne-2026.csv").bufferedReader().use(BufferedReader::readText)

    fun enregistrerFeuilleImportee(csv: String) { fichierFeuille.writeText(csv) }

    fun effacerFeuilleImportee() { fichierFeuille.delete() }

    fun lireResultats(): MutableList<Resultat> {
        if (!fichierResultats.exists()) return mutableListOf()
        val arr = JSONArray(fichierResultats.readText())
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Resultat(
                idCanonique = o.getString("idCanonique"),
                idClub = o.optString("idClub"),
                modele = o.optString("modele"),
                mode = o.getString("mode"),
                lestHg = o.optInt("lestHg", 110),
                lecture = if (o.isNull("lecture")) null else o.getDouble("lecture"),
                hsVisuel = o.optBoolean("hsVisuel"),
                flottaEvalueeN = if (o.isNull("flottaEvalueeN")) null else o.getDouble("flottaEvalueeN"),
                deltaN = if (o.isNull("deltaN")) null else o.getDouble("deltaN"),
                verdict = o.getString("verdict"),
                horodatage = o.getString("horodatage"),
            )
        }.toMutableList()
    }

    fun enregistrerResultats(resultats: List<Resultat>) {
        val arr = JSONArray()
        resultats.forEach { r ->
            arr.put(JSONObject().apply {
                put("idCanonique", r.idCanonique)
                put("idClub", r.idClub)
                put("modele", r.modele)
                put("mode", r.mode)
                put("lestHg", r.lestHg)
                put("lecture", r.lecture ?: JSONObject.NULL)
                put("hsVisuel", r.hsVisuel)
                put("flottaEvalueeN", r.flottaEvalueeN ?: JSONObject.NULL)
                put("deltaN", r.deltaN ?: JSONObject.NULL)
                put("verdict", r.verdict)
                put("horodatage", r.horodatage)
            })
        }
        fichierResultats.writeText(arr.toString())
    }

    fun maintenant(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    /**
     * Export CSV au format exact des campagnes + colonne Horodatage.
     * Test = « - » pour un HS sans lecture (non conforme d'office).
     */
    fun exportCsv(resultats: List<Resultat>): String {
        val sb = StringBuilder()
        sb.append(Csv.ENTETE.joinToString(";")).append(";Horodatage\n")
        resultats.forEach { r ->
            val test = when {
                r.hsVisuel -> "-"
                r.mode == Protocole.MODE_ANCIEN -> r.lecture?.let { (it * 10).toInt().toString() } ?: "-"
                else -> r.lecture?.let { String.format("%.1f", it) } ?: "-"
            }
            fun q(v: String) = if (v.contains(';')) "\"$v\"" else v
            sb.append(listOf(
                q(r.modele), r.idCanonique, r.idClub, test,
                r.lestHg.toString(),
                "", // Flotta (N) nominale : reprise à la transcription
                r.flottaEvalueeN?.let { String.format("%.1f", it) } ?: "",
                r.deltaN?.let { String.format("%.1f", it) } ?: "",
                r.verdict, r.horodatage,
            ).joinToString(";")).append('\n')
        }
        return sb.toString()
    }
}


