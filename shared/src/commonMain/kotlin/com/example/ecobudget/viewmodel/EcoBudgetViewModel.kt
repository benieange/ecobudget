package com.example.ecobudget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecobudget.data.repository.FakeTransactionRepository
import com.example.ecobudget.data.repository.TransactionRepository
import com.example.ecobudget.domain.model.Category
import com.example.ecobudget.domain.model.Transaction
import com.example.ecobudget.domain.model.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.random.Random

/**
 * Classe de données immuable représentant l'état complet de l'interface pour EcoBudget.
 */
data class EcoBudgetUiState(
    val currentMonth: YearMonth = YearMonth.current(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val monthTransactions: List<Transaction> = emptyList(),
    val allTransactions: List<Transaction> = emptyList(),
    val selectedCategories: Set<Category> = emptySet(),
    val monthlyBudget: Double = 500000.0,
    val totalSpent: Double = 0.0,
    val categorySpent: Double = 0.0,
    val remainingBudget: Double = 500000.0,
    val isAddDialogOpen: Boolean = false,
    val editingTransaction: Transaction? = null
) {
    /**
     * Indique si toutes les catégories sont actuellement sélectionnées / affichées.
     */
    val isAllCategoriesSelected: Boolean
        get() = selectedCategories.isEmpty() || selectedCategories.size == Category.entries.size

    /**
     * Ratio de consommation du budget mensuel (entre 0.0 et 1.0 ou supérieur).
     */
    val budgetUsageRatio: Float
        get() = if (monthlyBudget > 0) (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f

    /**
     * Pourcentage entier de consommation du budget mensuel.
     */
    val budgetUsagePercentage: Int
        get() = if (monthlyBudget > 0) ((totalSpent / monthlyBudget) * 100).toInt() else 0
}

/**
 * ViewModel responsable de la couche logique, de la navigation mensuelle et de l'état réactif d'EcoBudget.
 *
 * @param repository Dépôt de données pour les transactions.
 */
class EcoBudgetViewModel(
    private val repository: TransactionRepository = FakeTransactionRepository()
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.current())
    private val _selectedCategories = MutableStateFlow<Set<Category>>(emptySet())
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _editingTransaction = MutableStateFlow<Transaction?>(null)
    private val _monthlyBudget = MutableStateFlow(500000.0)

    // Combinaison des flux de filtrage
    private val _monthAndCategoriesFlow = combine(
        _currentMonth,
        _selectedCategories,
        _monthlyBudget
    ) { currentMonth, selectedCategories, monthlyBudget ->
        Triple(currentMonth, selectedCategories, monthlyBudget)
    }

    private val _dialogStateFlow = combine(
        _isAddDialogOpen,
        _editingTransaction
    ) { isAddDialogOpen, editingTransaction ->
        isAddDialogOpen to editingTransaction
    }

    /**
     * Flux d'état réactif public combinant les transactions, le mois courant,
     * la sélection multi-catégories et les dialogues.
     */
    val uiState: StateFlow<EcoBudgetUiState> = combine(
        repository.getTransactions(),
        _monthAndCategoriesFlow,
        _dialogStateFlow
    ) { transactions, monthAndCats, dialogs ->
        val currentMonth = monthAndCats.first
        val selectedCategories = monthAndCats.second
        val monthlyBudget = monthAndCats.third
        val isAddDialogOpen = dialogs.first
        val editingTransaction = dialogs.second

        // 1. Filtrer les transactions par le mois courant sélectionné
        val monthTxs = transactions.filter { currentMonth.containsTimestamp(it.date) }

        // 2. Filtrer par les catégories actives (si vide ou toutes, afficher tout le mois)
        val filtered = if (selectedCategories.isEmpty() || selectedCategories.size == Category.entries.size) {
            monthTxs
        } else {
            monthTxs.filter { it.category in selectedCategories }
        }

        val total = monthTxs.sumOf { it.amount }
        val catSpent = filtered.sumOf { it.amount }
        val remaining = (monthlyBudget - total).coerceAtLeast(0.0)

        EcoBudgetUiState(
            currentMonth = currentMonth,
            filteredTransactions = filtered,
            monthTransactions = monthTxs,
            allTransactions = transactions,
            selectedCategories = selectedCategories,
            monthlyBudget = monthlyBudget,
            totalSpent = total,
            categorySpent = catSpent,
            remainingBudget = remaining,
            isAddDialogOpen = isAddDialogOpen,
            editingTransaction = editingTransaction
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = EcoBudgetUiState()
    )

    /**
     * Navigue vers le mois précédent.
     */
    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.previous()
    }

    /**
     * Navigue vers le mois suivant.
     */
    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.next()
    }

    /**
     * Réinitialise la navigation sur le mois courant.
     */
    fun goToCurrentMonth() {
        _currentMonth.value = YearMonth.current()
    }

    /**
     * Bascule la sélection d'une catégorie (support multi-sélection).
     */
    fun toggleCategory(category: Category) {
        val currentSet = _selectedCategories.value
        val newSet = if (currentSet.contains(category)) {
            currentSet - category
        } else {
            currentSet + category
        }
        _selectedCategories.value = newSet
    }

    /**
     * Réinitialise le filtre pour afficher toutes les catégories ("Tous").
     */
    fun clearCategoryFilter() {
        _selectedCategories.value = emptySet()
    }

    /**
     * Ouvre la boîte de dialogue pour créer une nouvelle transaction.
     */
    fun openAddDialog() {
        _editingTransaction.value = null
        _isAddDialogOpen.value = true
    }

    /**
     * Ouvre la boîte de dialogue pré-remplie pour modifier une transaction existante.
     */
    fun openEditDialog(transaction: Transaction) {
        _editingTransaction.value = transaction
        _isAddDialogOpen.value = true
    }

    /**
     * Ferme la boîte de dialogue d'ajout / édition.
     */
    fun dismissDialog() {
        _isAddDialogOpen.value = false
        _editingTransaction.value = null
    }

    /**
     * Enregistre ou met à jour une dépense selon le contexte d'édition.
     */
    fun saveTransaction(title: String, amount: Double, category: Category) {
        if (title.isBlank() || amount <= 0.0) return

        val currentEditing = _editingTransaction.value

        viewModelScope.launch {
            if (currentEditing != null) {
                // Modification d'une transaction existante
                val updated = currentEditing.copy(
                    title = title.trim(),
                    amount = amount,
                    category = category
                )
                repository.updateTransaction(updated)
            } else {
                // Création d'une nouvelle transaction
                val dateToUse = Clock.System.now().toEpochMilliseconds()

                val newTransaction = Transaction(
                    id = generateUniqueId(),
                    title = title.trim(),
                    amount = amount,
                    date = dateToUse,
                    category = category
                )
                repository.addTransaction(newTransaction)
            }
            dismissDialog()
        }
    }

    /**
     * Supprime une dépense par son identifiant unique.
     */
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    /**
     * Génère un identifiant unique compatible Kotlin Pure/KMP.
     */
    private fun generateUniqueId(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "$timestamp-${Random.nextLong(100000, 999999)}"
    }
}