plugins {
    kotlin("multiplatform")
    id("com.android.library")
// Support de Compose Multiplatform et génération de l'objet Res
    id("org.jetbrains.compose") version "1.6.11"
// Compilateur Compose obligatoire depuis Kotlin 2.0+
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Cibles iOS avec génération du framework binaire
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    // Dépendances communes
    sourceSets {
        commonMain.dependencies {
// Asynchronisme et flux réactifs
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

// Gestion de l'état UI et cycle de vie multiplateforme
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.0")

// Manipulation multiplateforme des dates et heures
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
// Ressources CMP (exposées à app) et Runtime Compose
            api(compose.components.resources)
            implementation(compose.runtime)
        }
    }


}

// Configuration minimale requise par le plugin com.android.library
android {
    namespace = "com.example.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Autorise le module Android 'app' à consommer l'objet Res
compose.resources {
    publicResClass = true
}