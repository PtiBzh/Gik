plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "fr.ydel.gilets"
    compileSdk = 34

    defaultConfig {
        applicationId = "fr.ydel.gilets"   // IMPOSÉ — ne pas changer (cahier des charges)
        minSdk = 26                        // Crosscall Core-Z5 = Android 12 (API 31), marge descendante
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Keystore : le générer et le conserver dès le PREMIER build signé.
    // Voir README.md — ne jamais le perdre.
    //
    // Signature CI (GitHub Actions) : les variables d'environnement sont
    // fournies par le workflow ; sans keystore, l'APK release reste non
    // signé et l'APK debug reste utilisable tel quel.
    signingConfigs {
        create("ci") {
            System.getenv("KEYSTORE_FILE")?.let { storeFile = file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (System.getenv("KEYSTORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("ci")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.core:core-ktx:1.13.1")

    // Scan code-barres (gilets Silh) — embarqué, aucune dépendance Play Services.
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}


