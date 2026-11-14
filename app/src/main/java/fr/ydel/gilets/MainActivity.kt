package fr.ydel.gilets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import fr.ydel.gilets.data.*
import fr.ydel.gilets.ui.*
import java.io.File

/**
 * Test gilets CNPA — saisie au bac. Portage Kotlin natif de la web app.
 * Hors ligne, aucune dépendance réseau. applicationId imposé : fr.ydel.gilets.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    @Composable
    private fun App() {
        val context = LocalContext.current
        val store = remember { Store(context) }

        // ---- État ----
        var gilets by remember { mutableStateOf(listOf<Gilet>()) }
        var resultats by remember { mutableStateOf(store.lireResultats()) }
        var mode by remember { mutableStateOf(Protocole.MODE_POULIE) } // poulie = défaut
        var recherche by remember { mutableStateOf("") }
        var selection by remember { mutableStateOf<Gilet?>(null) }
        var lecture by remember { mutableStateOf("") }
        var verdictAffiche by remember { mutableStateOf<Resultat?>(null) }
        var message by remember { mutableStateOf<String?>(null) }

        // Chargement initial : feuille importée sinon asset 2026 embarqué.
        LaunchedEffect(Unit) { gilets = Csv.parse(store.chargerFeuilleDefaut(context)) }

        fun valide(g: Gilet?): Boolean {
            if (g == null) { message = "Aucun gilet sélectionné — scanne ou saisis un n°."; return false }
            val kg = lecture.replace(',', '.').toDoubleOrNull()
            if (kg == null || kg < 0) { message = "Lecture peson invalide."; return false }
            val flotta = Protocole.flottabiliteN(mode, kg, g.lestHg)
            val verdict = Protocole.verdict(flotta, g.flottaNominaleN)
            val r = Resultat(
                idCanonique = g.idCanonique, idClub = g.idClub, modele = g.modele,
                mode = mode, lestHg = g.lestHg, lecture = kg, hsVisuel = false,
                flottaEvalueeN = flotta, deltaN = flotta - g.flottaNominaleN,
                verdict = verdict, horodatage = store.maintenant(),
            )
            resultats = (resultats.filter { it.idCanonique != r.idCanonique } + r).toMutableList()
            store.enregistrerResultats(resultats)
            verdictAffiche = r
            lecture = ""
            return true
        }

        fun hsSansLecture(g: Gilet?) {
            if (g == null) { message = "Aucun gilet sélectionné."; return }
            val r = Resultat(
                idCanonique = g.idCanonique, idClub = g.idClub, modele = g.modele,
                mode = mode, lestHg = g.lestHg, lecture = null, hsVisuel = true,
                flottaEvalueeN = null, deltaN = null,
                verdict = "HS", horodatage = store.maintenant(),
            )
            resultats = (resultats.filter { it.idCanonique != r.idCanonique } + r).toMutableList()
            store.enregistrerResultats(resultats)
            verdictAffiche = r
            lecture = ""
        }

        // ---- Launchers ----
        val scan = rememberLauncherForActivityResult(ScanContract()) { resultat ->
            resultat.contents?.let { code ->
                recherche = code
                val g = Csv.cherche(gilets, code)
                if (g == null) message = "Code non reconnu dans la feuille : $code"
                selection = g
                verdictAffiche = null
            }
        }
        val importCsv = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                runCatching {
                    val csv = context.contentResolver.openInputStream(uri)!!
                        .bufferedReader().readText()
                    val parsed = Csv.parse(csv)
                    require(parsed.isNotEmpty()) { "CSV illisible ou vide" }
                    store.enregistrerFeuilleImportee(csv)
                    gilets = parsed
                    message = "Feuille importée : ${parsed.size} gilets. L'APK n'a pas besoin d'être recompilé."
                }.onFailure { message = "Import échoué : ${it.message}" }
            }
        }
        val exportCsv = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)!!.use { os ->
                        os.write(store.exportCsv(resultats).toByteArray(Charsets.UTF_8))
                    }
                    message = "Export CSV écrit (${resultats.size} lignes)."
                }.onFailure { message = "Export échoué : ${it.message}" }
            }
        }

        // ---- UI ----
        MaterialTheme(colorScheme = darkColorScheme()) {
            Column(
                Modifier.background(BG).fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Test gilets CNPA", color = TEXTE, fontSize = 26.sp, fontWeight = FontWeight.Black)
                BarreProgres(testes = resultats.size, total = gilets.size)

                SelecteurMode(mode) { mode = it; verdictAffiche = null }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = recherche,
                        onValueChange = { s ->
                            recherche = s
                            selection = Csv.cherche(gilets, s)
                            verdictAffiche = null
                        },
                        label = { Text("N° gilet (scan ou saisie)", color = TEXTE_ATTENU) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = TEXTE, fontSize = 20.sp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ACCENT, unfocusedBorderColor = TEXTE_ATTENU,
                            cursorColor = ACCENT,
                        ),
                    )
                    Button(
                        onClick = {
                            scan.launch(
                                ScanOptions().apply {
                                    setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                                    setPrompt("Visez le code-barres du gilet")
                                    setBeepEnabled(true)
                                    setOrientationLocked(true)
                                }
                            )
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = BG),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.height(64.dp),
                    ) { Text("SCAN", fontWeight = FontWeight.Black, fontSize = 16.sp) }
                }

                selection?.let { g -> CarteGilet(g.modele, g.idCanonique, g.idClub, g.flottaNominaleN, g.lestHg) }

                ChampLecture(lecture) { lecture = it }

                verdictAffiche?.let { r ->
                    val nominale = gilets.firstOrNull { it.idCanonique == r.idCanonique }?.flottaNominaleN
                    val details = if (r.hsVisuel)
                        "HS sans lecture — ${if (r.mode == Protocole.MODE_POULIE) "coule" else "flotte"} (visuel). Non conforme d'office."
                    else {
                        val f = r.flottaEvalueeN ?: 0.0
                        val d = r.deltaN ?: 0.0
                        "Flottabilité évaluée : %.1f N — nominale ${nominale ?: "?"} N — Δ : %+.1f N".format(f, d)
                    }
                    BandeauVerdict(r.verdict, details)
                }

                BoutonGeant("VALIDER LE TEST", { valide(selection) })
                BoutonGeant(
                    Protocole.libelleHs(mode),
                    { hsSansLecture(selection) },
                    fond = HS_FOND, texteCouleur = HS_TEXTE,
                )

                message?.let {
                    Text(it, color = ACCENT, fontSize = 15.sp)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BoutonGeant("IMPORTER\nFEUILLE CSV", { importCsv.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel")) }, modifier = Modifier.weight(1f))
                    BoutonGeant("EXPORTER\nRÉSULTATS CSV", { exportCsv.launch("campagne-gilets-resultats.csv") }, modifier = Modifier.weight(1f))
                }
                BoutonGeant(
                    "RÉINITIALISER LES RÉSULTATS",
                    {
                        resultats = mutableListOf()
                        store.enregistrerResultats(resultats)
                        verdictAffiche = null
                        message = "Résultats effacés."
                    },
                    texteCouleur = TEXTE_ATTENU,
                )
            }
        }
    }
}


