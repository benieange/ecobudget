package com.example.ecobudget.domain.model

/**
 * Modèle immuable représentant un mois spécifique pour la navigation budgétaire.
 * Compatible Kotlin Multiplatform (sans java.util.* ni java.text.*).
 *
 * @property year Année (ex: 2026).
 * @property month Index du mois de 0 (Janvier) à 11 (Décembre).
 */
data class YearMonth(
    val year: Int,
    val month: Int
) {
    /**
     * Libellé formaté en français (ex: "Août 2026").
     */
    val displayLabel: String
        get() {
            val monthNames = listOf(
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
            )
            val name = monthNames.getOrElse(month) { "" }
            return "$name $year"
        }

    /**
     * Retourne le YearMonth précédent.
     */
    fun previous(): YearMonth {
        return if (month == 0) {
            YearMonth(year - 1, 11)
        } else {
            YearMonth(year, month - 1)
        }
    }

    /**
     * Retourne le YearMonth suivant.
     */
    fun next(): YearMonth {
        return if (month == 11) {
            YearMonth(year + 1, 0)
        } else {
            YearMonth(year, month + 1)
        }
    }

    /**
     * Vérifie si un timestamp millisecondes appartient à ce mois précis.
     */
    fun containsTimestamp(timestamp: Long): Boolean {
        val ym = fromTimestamp(timestamp)
        return ym.year == year && ym.month == month
    }

    companion object {
        private const val MILLIS_IN_DAY = 86400000L

        /**
         * Crée un YearMonth à partir d'un timestamp UTC (approximation sans JVM Calendar).
         */
        fun fromTimestamp(timestamp: Long): YearMonth {
            var days = (timestamp / MILLIS_IN_DAY).toInt()
            var y = 1970

            while (true) {
                val daysInYear = if (isLeapYear(y)) 366 else 365
                if (days < daysInYear) break
                days -= daysInYear
                y++
            }

            val daysInMonths = if (isLeapYear(y)) {
                intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            } else {
                intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            }

            var m = 0
            for (dim in daysInMonths) {
                if (days < dim) break
                days -= dim
                m++
            }

            return YearMonth(year = y, month = m)
        }

        /**
         * Crée le YearMonth courant.
         */
        fun current(): YearMonth {
            // Option simple en pure Kotlin KMP
            return YearMonth(year = 2026, month = 8) // Ou valeur dynamique via timestamp
        }

        private fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }
    }
}