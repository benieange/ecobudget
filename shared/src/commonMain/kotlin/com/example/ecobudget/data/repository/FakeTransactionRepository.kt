package com.example.ecobudget.data.repository

import com.example.ecobudget.domain.model.Category
import com.example.ecobudget.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implémentation factice (Mock/In-Memory) de [TransactionRepository] neutre pour Kotlin Multiplatform.
 */
class FakeTransactionRepository : TransactionRepository {

    private val _transactionsFlow: MutableStateFlow<List<Transaction>>

    init {
        // Base de temps UTC fixe neutre (ex: Septembre 2026)
        val baseTimestamp = 1788200000000L
        val monthInMillis = 30L * 24L * 60L * 60L * 1000L

        fun getTimeForMonth(monthOffset: Int, dayOffset: Int): Long {
            return baseTimestamp + (monthOffset * monthInMillis) + (dayOffset * 86400000L)
        }

        val initialList = listOf(
            // Mois actuel (0)
            Transaction(
                id = "tx_1",
                title = "Supermarché Bio",
                amount = 45000.0,
                date = getTimeForMonth(0, 5),
                category = Category.ALIMENTATION
            ),
            Transaction(
                id = "tx_2",
                title = "Session Tennis",
                amount = 12000.0,
                date = getTimeForMonth(0, 3),
                category = Category.LOISIRS
            ),
            Transaction(
                id = "tx_3",
                title = "Ticket de Bus Express",
                amount = 2500.0,
                date = getTimeForMonth(0, 2),
                category = Category.TRANSPORT
            ),
            Transaction(
                id = "tx_4",
                title = "Loyer Mensuel",
                amount = 250000.0,
                date = getTimeForMonth(0, 0),
                category = Category.LOGEMENT
            ),
            Transaction(
                id = "tx_5",
                title = "Boulangerie & Pâtisserie",
                amount = 4800.0,
                date = getTimeForMonth(0, 1),
                category = Category.ALIMENTATION
            ),
            Transaction(
                id = "tx_6",
                title = "Recharge Vélo Électrique",
                amount = 3500.0,
                date = getTimeForMonth(0, 4),
                category = Category.TRANSPORT
            ),
            Transaction(
                id = "tx_7",
                title = "Facture Électricité",
                amount = 48000.0,
                date = getTimeForMonth(0, 2),
                category = Category.LOGEMENT
            ),

            // Mois précédent (-1)
            Transaction(
                id = "tx_8",
                title = "Loyer Mois Précédent",
                amount = 250000.0,
                date = getTimeForMonth(-1, 0),
                category = Category.LOGEMENT
            ),
            Transaction(
                id = "tx_9",
                title = "Courses du mois",
                amount = 65000.0,
                date = getTimeForMonth(-1, 5),
                category = Category.ALIMENTATION
            ),
            Transaction(
                id = "tx_10",
                title = "Abonnement Transport",
                amount = 35000.0,
                date = getTimeForMonth(-1, 2),
                category = Category.TRANSPORT
            ),
            Transaction(
                id = "tx_11",
                title = "Sortie Restaurant",
                amount = 22000.0,
                date = getTimeForMonth(-1, 10),
                category = Category.LOISIRS
            ),

            // Mois suivant (+1)
            Transaction(
                id = "tx_12",
                title = "Avance Loyer Prévue",
                amount = 250000.0,
                date = getTimeForMonth(1, 0),
                category = Category.LOGEMENT
            ),
            Transaction(
                id = "tx_13",
                title = "Abonnement Salle de Sport",
                amount = 20000.0,
                date = getTimeForMonth(1, 3),
                category = Category.LOISIRS
            )
        )

        _transactionsFlow = MutableStateFlow(initialList)
    }

    override fun getTransactions(): Flow<List<Transaction>> {
        return _transactionsFlow.asStateFlow()
    }

    override suspend fun addTransaction(transaction: Transaction) {
        _transactionsFlow.update { currentList ->
            listOf(transaction) + currentList
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        _transactionsFlow.update { currentList ->
            currentList.map { if (it.id == transaction.id) transaction else it }
        }
    }

    override suspend fun deleteTransaction(id: String) {
        _transactionsFlow.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }
}