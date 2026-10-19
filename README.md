# Test gilets CNPA

Application Android native pour le test des gilets de flottabilité du Club Nautique de Lesconil (CNPA).

## Contexte

Chaque hiver, environ 230 gilets de flottabilité en mousse sont testés individuellement au bac d'eau par une seule personne, en une session, en conditions humides. Cette application remplace le flux actuel (saisie sur tableau Véléda → photo → retranscription CSV) par une saisie directe sur smartphone Android, hors ligne.

## Fonctionnalités

- **Saisie rapide** : Scan de code-barres ou saisie manuelle du numéro de gilet
- **Reconnaissance intelligente** : Recherche dans la feuille de campagne embarquée avec normalisation des ID
- **Calcul automatique** : Calcul de la flottabilité et du verdict selon le mode de mesure
- **Verdict clair** : Affichage plein écran avec couleur, texte et vibration
- **Gestion de session** : Liste des tests, export CSV, statistiques
- **100% hors ligne** : Aucune connexion réseau requise
- **Mode avion compatible** : Fonctionne entièrement sans réseau

## Prérequis

- Android Studio (version 2023.2 ou supérieure)
- SDK Android 34
- Java JDK 17
- Appareil Android avec API 26+ (Android 8+)

## Installation

### 1. Cloner le projet

```bash
git clone https://github.com/PtiBzh/Gik.git
cd Gik
```

### 2. Configurer le SDK Android

Assurez-vous d'avoir installé le SDK Android 34 via le SDK Manager d'Android Studio.

### 3. Builder l'APK

```bash
# APK de debug
./gradlew assembleDebug

# APK de release
./gradlew assembleRelease
```

L'APK sera générée dans `app/build/outputs/apk/`.

### 4. Installer sur un appareil

```bash
# Installer l'APK debug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Structure du projet

```
app/
├── src/
│   ├── main/
│   │   ├── java/fr/ydel/gilets/
│   │   │   ├── data/           # Modèles et accès aux données
│   │   │   ├── ui/             # Activités et adaptateurs
│   │   │   ├── util/           # Classes utilitaires
│   │   │   ├── viewmodel/      # ViewModels
│   │   │   └── TestGiletsApp.kt # Application
│   │   ├── res/               # Ressources
│   │   │   ├── layout/         # Layouts XML
│   │   │   ├── values/         # Strings, colors, styles
│   │   │   └── assets/         # Fichiers d'actifs (CSV)
│   │   └── AndroidManifest.xml
│   └── test/                  # Tests unitaires
└── build.gradle.kts            # Configuration Gradle
```

## Fichier de campagne

### Format CSV

Le fichier de campagne doit être au format CSV avec les colonnes suivantes (séparateur `;`):

```csv
Modèle;ID canonique;ID club;Test;Lest;Flotta (N);Constat
Gilet Adulte;FAB001;23-001;;;200,0;
Gilet Adulte;FAB002;23-002;;;200,0;
```

**Colonnes:**
1. Modèle : Nom du modèle de gilet
2. ID canonique : Identifiant fabricant
3. ID club : Identifiant interne du club (ex: "23-001")
4. Test : (optionnel) Information de test
5. Lest : (optionnel) Valeur du lest en kg
6. **Flotta (N)** : **Seuil de flottabilité nominale en Newtons** (requis)
7. Constat : (optionnel) Constat initial

### Intégration par défaut

Le fichier `campagne-gilets-2026.csv` est intégré en tant qu'asset. Pour le mettre à jour:

1. Placer le fichier CSV dans `app/src/main/assets/`
2. Rebuild l'application

### Import à chaud

Depuis l'écran des réglages, vous pouvez importer un nouveau fichier de campagne via le sélecteur de fichiers Android. Les tests déjà enregistrés dans la session sont conservés.

## Modes de mesure

### Banc poulie (défaut)
La flottabilité est lue directement :
```
Flottabilité (N) = lecture_kg × 9,8
```

### Ancien protocole
La flottabilité est calculée à partir du lest :
```
Flottabilité (N) = (lest_kg − lecture_kg) × 9,8
```

## Verdicts

- **Conforme** : flottabilité ≥ seuil nominal
- **Non conforme (flottabilité)** : flottabilité < seuil nominal
- **Non conforme (visuel)** : détection visuelle sans mesure
- **À vérifier (seuil inconnu)** : gilet hors liste sans seuil manuel

## Export CSV

Le format d'export respect les colonnes des campagnes :

```csv
Modèle;ID canonique;ID club;Test (Visu ou ×100g);Lest (×100g);Flotta (N);Flotta évaluée N;Δ (N);Constat;Horodatage
```

- `Test` : lecture en hectogrammes (kg × 100, arrondi) ou `-` si sans lecture
- `Lest` : 110 (hectogrammes) en mode ancien, vide en mode poulie
- `Flotta évaluée N` : flottabilité calculée
- `Δ (N)` : différence par rapport au seuil
- `Horodatage` : format `AAAA-MM-JJ hh:mm:ss`

## Configuration

### Identifiant d'application
- **ID** : `fr.ydel.gilets`
- **Nom** : "Test gilets"

### Permissions
- `CAMERA` : Pour le scan des codes-barres
- `VIBRATE` : Pour les retours haptiques

### Keystore de release

Pour générer un keystore de release :

```bash
keytool -genkey -v -keystore release.keystore -alias gilets -keyalg RSA -keysize 2048 -validity 10000
```

Placer le fichier `release.keystore` dans le répertoire `app/` et configurer le `app/build.gradle.kts` :

```kotlin
android {
    signingConfigs {
        release {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "gilets"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## Tests

Exécuter les tests unitaires :

```bash
./gradlew test
```

Les tests couvrent :
- Calcul des verdicts selon les règles métier
- Parsing et validation du CSV
- Export CSV
- Normalisation des ID

## Compatibilité

- **Min SDK** : Android 8 (API 26)
- **Target SDK** : Android 14 (API 34)
- **Appareil testé** : Crosscall Core-Z5 (Android 12-14)

## Contribution

Le projet est FOSS-ready. Pour contribuer :

1. Forker le dépôt
2. Créer une branche de fonctionnalité
3. Commiter vos modifications
4. Ouvrir une Pull Request

## Licence

Le projet utilise des dépendances libres (Apache-2.0, MIT). Le code est prêt pour une publication sur F-Droid.

## Auteurs

Développé pour le Club Nautique de Lesconil (CNPA).

## Remerciements

- zxing-android-embedded pour le scan de codes-barres
- Android Jetpack pour l'architecture
- Room pour la persistance locale
