# Test gilets CNPA — application Android native

Portage Kotlin natif de la web app « Test gilets CNPA — saisie au bac ».
Conforme au cahier des charges « Scope — Application Android native ».

## Contraintes imposées (cahier des charges)

- `applicationId` : **fr.ydel.gilets** — ne jamais changer.
- Nom visible : **Test gilets**.
- **Hors ligne** : aucune permission Internet, aucun Play Services.
- Scan code-barres : **zxing-android-embedded** (gilets Silh — le code scanné
  est recherché en correspondance exacte OU comme contenu de la valeur lue ;
  l'ID club, ex. 55-25, matche aussi).
- Téléphone de test : **Crosscall Core-Z5** (Android 12, écran 500 nits,
  tactile mouillé/ganté) → UI très contrastée, gros boutons, jamais
  d'information portée par la couleur seule (symboles ✓/✗ + mots OK/HS).
- **Keystore à générer et conserver dès le PREMIER build signé.**

## Deux modes de protocole

- **Banc poulie** (défaut) : la lecture peson (kg) EST la flottabilité → N = kg × 9,8.
- **Ancien protocole** : flottabilité = (Lest − Test) × 9,8, Test = lecture en ×100 g.
- Boutons **HS sans lecture** : « HS — coule » en mode poulie, « HS — flotte »
  en ancien → « - » dans la colonne Test, non conforme d'office.

## Feuille de campagne

- L'asset `campagne-2026.csv` (231 gilets, feuille pré-remplie 2026) est
  embarqué par défaut.
- **L'APK n'est jamais recompilé** : pour la campagne suivante, régénérer la
  feuille CSV puis l'importer dans l'app (bouton IMPORTER — sélecteur de
  fichiers SAF). Format : séparateur `;`, UTF-8 avec BOM, colonnes
  `Modèle;ID canonique;ID club;Test;Lest (×100g);Flotta (N);…`.

## Résultats et export

- Stockage local (équivalent localStorage) : `filesDir/resultats.json`.
- Un gilet re-testé remplace sa ligne précédente.
- EXPORTER écrit le **format exact des campagnes + colonne Horodatage**
  via « Créer un document » (SAF) — à ouvrir dans le CSV de campagne.

## Build

1. Ouvrir le dossier dans **Android Studio** (Koala ou plus récent).
2. Laisser Gradle synchroniser (JDK 17 requis).
3. `Build > Generate Signed Bundle/APK` → **créer le keystore maintenant**
   et l'archiver (perte = fin des mises à jour pour les installations
   existantes). Sideload APK sur le Core-Z5.

## Compilation par GitHub (GitHub Actions)

À chaque push sur `main` (ou déclenchement manuel dans l'onglet **Actions**),
`.github/workflows/build-apk.yml` compile le projet sur une machine GitHub et
publie l'APK en **artefact téléchargeable** (30 jours de rétention) :

- **apk-debug** : toujours produit, installable directement — aucun réglage.
- **apk-release** : signé, uniquement si les secrets du dépôt sont définis.

Mise en place de la signature release (une seule fois) :

1. Générer le keystore **et le conserver hors du dépôt** (perte = pas de
   mise à jour possible pour les APK déjà installés) :
   `keytool -genkey -v -keystore test-gilets.keystore -alias gilets -keyalg RSA -keysize 2048 -validity 10000`
2. Dans Settings > Secrets and variables > Actions du dépôt, ajouter :
   - `KEYSTORE_B64` : `base64 -w0 test-gilets.keystore` (le keystore encodé)
   - `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
3. Pousser : l'APK signé apparaît en artefact du run.

Note : le keystore ne transite jamais en clair ; il est décodé uniquement
sur la machine de build, jetée après le run.

Distribution Play Store / F-Droid : possible plus tard, pas pour l'hiver.

## Structure



