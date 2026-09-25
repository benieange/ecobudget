package com.example.ecobudget.domain.model

/**
 * Représente les catégories obligatoires pour la classification des dépenses dans EcoBudget.
 * Rendu neutre pour Kotlin Multiplatform (plus de dépendance à Android R.string).
 */
enum class Category(
    val titleKey: String, // Clé de texte neutre ou identifiant
    val emoji: String
) {
    TRANSPORT("category_transport", "🚌"),
    ALIMENTATION("category_alimentation", "🍱"),
    LOISIRS("category_loisirs", "🎾"),
    LOGEMENT("category_logement", "🏠")
}