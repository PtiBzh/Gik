package fr.ydel.gilets.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Palette très contrastée pour l'écran 500 nits du Crosscall Core-Z5.
// Règle « pas d'info couleur seule » : tout verdict s'accompagne d'un
// symbole (✓ / ✗) et d'un mot (OK / HS).

val BG = Color(0xFF101418)
val CARTE = Color(0xFF1B2129)
val TEXTE = Color(0xFFF2F5F7)
val TEXTE_ATTENU = Color(0xFF9AA7B0)
val ACCENT = Color(0xFF4FC3F7)
val OK_FOND = Color(0xFF123D22)
val OK_TEXTE = Color(0xFF7CFC9B)
val HS_FOND = Color(0xFF4A1414)
val HS_TEXTE = Color(0xFFFF8A80)

@Composable
fun BoutonGeant(
    texte: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fond: Color = CARTE,
    texteCouleur: Color = TEXTE,
    actif: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = actif,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (actif) fond else CARTE,
            contentColor = if (actif) texteCouleur else TEXTE_ATTENU,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp),
    ) {
        Text(texte, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun BandeauVerdict(verdict: String, details: String) {
    val ok = verdict == "OK"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (ok) OK_FOND else HS_FOND, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                if (ok) "✓ OK — conforme" else "✗ HS — réformé",
                color = if (ok) OK_TEXTE else HS_TEXTE,
                fontSize = 24.sp, fontWeight = FontWeight.Black,
            )
            if (details.isNotEmpty()) {
                Text(details, color = TEXTE, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CarteGilet(modele: String, idCanonique: String, idClub: String, nominaleN: Int, lestHg: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CARTE, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(modele, color = TEXTE, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "ID $idCanonique" + (if (idClub.isNotEmpty()) "  ·  club $idClub" else ""),
            color = ACCENT, fontSize = 16.sp,
        )
        Text(
            "Flottabilité nominale : $nominaleN N   ·   Lest : $lestHg",
            color = TEXTE_ATTENU, fontSize = 14.sp,
        )
    }
}

@Composable
fun ChampLecture(kg: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = kg,
        onValueChange = { s -> onChange(s.filter { it.isDigit() || it == ',' || it == '.' }) },
        label = { Text("Lecture peson (kg)", color = TEXTE_ATTENU) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            color = TEXTE, fontSize = 32.sp, fontWeight = FontWeight.Bold,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ACCENT,
            unfocusedBorderColor = TEXTE_ATTENU,
            cursorColor = ACCENT,
        ),
    )
}

@Composable
fun SelecteurMode(mode: String, onChoix: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            "poulie" to "BANC POULIE (lecture = flottabilité)",
            "ancien" to "ANCIEN ((Lest − Test) × 9,8)",
        ).forEach { (m, libelle) ->
            val actif = mode == m
            Button(
                onClick = { onChoix(m) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (actif) ACCENT else CARTE,
                    contentColor = if (actif) BG else TEXTE,
                ),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.weight(1f).heightIn(min = 60.dp),
            ) {
                Text(libelle, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun BarreProgres(testes: Int, total: Int) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "$testes / $total testés",
            color = TEXTE_ATTENU, fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End,
        )
        LinearProgressIndicator(
            progress = { if (total > 0) testes.toFloat() / total else 0f },
            color = ACCENT, trackColor = CARTE,
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )
    }
}


