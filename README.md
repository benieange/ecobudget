# EcoBudget 🌿

Dépôt de base pour le projet du cours de développement mobile avancé.


RAPPORT TECHNIQUE DE MIGRATION : ECOBUDGET
================================================================================

1. CONTEXTE ET OBJECTIFS DU PROJET
--------------------------------------------------------------------------------
L'application EcoBudget est un prototype Android natif dédié à la gestion
de budget personnel. L'objectif principal de ce projet est de faire évoluer son
architecture initiale vers une solution multiplateforme basée sur Kotlin
Multiplatform (KMP) et Compose Multiplatform (CMP).

L'objectif final consiste à extraire la couche Domaine, la couche Données,
l'état de l'interface utilisateur, le ViewModel principal ainsi que les
ressources textuelles vers un module partagé (:shared), tout en maintenant
la compatibilité complète de l'application Android native lors de l'exécution.


2. INFRASTRUCTURE ET CONFIGURATION KMP (:shared)
--------------------------------------------------------------------------------
Afin d'accueillir le code partagé entre Android et iOS, l'infrastructure du
projet a été restructurée avec la création du module :shared.

Extrait de la configuration Gradle (shared/build.gradle.kts) :

plugins {
alias(libs.plugins.kotlinMultiplatform)
alias(libs.plugins.androidLibrary)
alias(libs.plugins.jetbrainsCompose)
alias(libs.plugins.compose.compiler)
}

kotlin {
androidTarget()

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

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.datetime)
        }
    }
}


3. DOCUMENT TECHNIQUE DE SYNTHÈSE (ANALYSE PAR FICHIER MIGRÉ)
--------------------------------------------------------------------------------

3.1 COUCHE DOMAINE (model)
--------------------------------------------------------------------------------
Fichiers concernés : Transaction.kt, Category.kt, BudgetSummary.kt

* Le problème rencontré :
  L'implémentation initiale s'appuyait sur la classe java.util.Date ainsi que
  java.util.UUID pour la gestion des identifiants et des horodatages. Ces
  classes appartiennent au SDK Java (JVM) et échouent lors de la compilation
  pour les cibles non-JVM (iOS/Native).

* Le choix technique appliqué :
  Remplacement de java.util.Date par la bibliothèque multiplateforme
  kotlinx-datetime (Instant ou LocalDate) et substitution de java.util.UUID
  par une génération d'identifiant sous forme de chaîne de caractères unique
  ou l'utilisation de kotlin.uuid.Uuid.

* La justification :
  kotlinx-datetime fournit une API purement Kotlin supportée sur l'ensemble
  des cibles KMP (Android, iOS, Desktop, Web), garantissant l'absence de
  dépendance vis-à-vis de l'environnement d'exécution de la JVM.


3.2 COUCHE DONNÉES ET REPOSITORIES (data)
--------------------------------------------------------------------------------
Fichier concerné : ExpenseRepository.kt

* Le problème rencontré :
  Le dépôt d'origine utilisait des types d'I/O Java (java.io.*), ainsi que
  des types réactifs spécifiques à Android/JVM (LiveData).

* Le choix technique appliqué :
  Remplacement des LiveData par des StateFlow et SharedFlow de la bibliothèque
  kotlinx.coroutines. Encapsulation des opérations asynchrones dans des
  fonctions suspendues (suspend fun).

* La justification :
  Les Flow de Kotlin Coroutines constituent le standard natif KMP pour le
  traitement de flux de données réactifs et asynchrones, sans aucune
  adhérence au cycle de vie Android.


3.3 COUCHE PRÉSENTATION (ui / viewmodel)
--------------------------------------------------------------------------------
Fichier concerné : MainViewModel.kt

* Le problème rencontré :
  La classe héritait directement de androidx.lifecycle.ViewModel du SDK
  Android Framework et utilisait viewModelScope dépendant de androidx.lifecycle.

* Le choix technique appliqué :
  Utilisation de la bibliothèque partagée androidx.lifecycle:lifecycle-viewmodel
  compatible KMP.

  Exemple d'implémentation dans commonMain :

  open class MainViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(EcoBudgetUiState())
  val uiState: StateFlow<EcoBudgetUiState> = _uiState.asStateFlow()

      fun addTransaction(transaction: Transaction) {
          viewModelScope.launch {
              // Logique d'ajout multiplateforme
          }
      }
  }

* La justification :
  Permet de conserver la logique de gestion d'état et le cycle de vie du
  ViewModel sur Android tout en rendant cette même logique utilisable sur iOS
  sans dupliquer les états d'interface (UiState).


3.4 CENTRALISATION DES RESSOURCES TEXTUELLES (resources)
--------------------------------------------------------------------------------
Fichiers concernés : Strings.kt / Compose Resources

* Le problème rencontré :
  L'application s'appuyait sur l'accès natif context.getString(R.string.app_name)
  ou stringResource(R.string.*) spécifique au système de ressources d'Android
  (res/values/strings.xml).

* Le choix technique appliqué :
  Mise en place de Compose Multiplatform Resources (org.jetbrains.compose.resources)
  ou d'un objet singleton centralisant les chaînes de caractères en Kotlin
  pur (Res.string.*).

* La justification :
  Cette approche permet de piloter l'ensemble de la localisation et des libellés
  depuis le module shared, rendant les textes accessibles à la fois à
  l'application Android et aux futurs développements iOS.


4. VALIDATION FONCTIONNELLE ET EXÉCUTION
--------------------------------------------------------------------------------
La phase de validation sur l'émulateur Android a permis de confirmer la
stabilité de l'application post-migration :

1. Calcul des dépenses & Tableau de bord :
   Les calculs de totaux et la mise à jour dynamique via StateFlow s'exécutent
   sans latence sur le thread principal.

2. Navigation Mensuelle & Filtrage :
   Le filtrage des transactions par date/catégorie fonctionne correctement avec
   la nouvelle gestion temporelle via kotlinx-datetime.

3. Absence de régression :
   L'application s'exécute sans exception au lancement (PROCESS STARTED),
   l'allocation mémoire est optimisée et l'UI réagit conformément aux attentes.


5. BILAN ET LIVRABLES
--------------------------------------------------------------------------------
* Code source complet : https://github.com/benieange/ecobudget
* Branche : master
  