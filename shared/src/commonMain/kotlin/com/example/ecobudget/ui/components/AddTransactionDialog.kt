package com.example.ecobudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecobudget.domain.model.Category
import com.example.ecobudget.domain.model.Transaction
import com.example.ecobudget.ui.theme.DarkDialogBackground
import com.example.ecobudget.ui.theme.DarkDialogChipInactive
import com.example.ecobudget.ui.theme.DarkDialogFieldBackground
import com.example.ecobudget.ui.theme.DarkDialogOutline
import com.example.ecobudget.ui.theme.DarkOutline
import com.example.ecobudget.ui.theme.DarkTextSecondary
import com.example.ecobudget.ui.theme.VioletPrimary
import com.example.ecobudget.ui.theme.VioletPrimaryLight

/**
 * Boîte de dialogue KMP permettant l'enregistrement ou la modification d'une dépense.
 *
 * @param initialTransaction Transaction à modifier si en mode édition, ou null si nouvelle dépense.
 * @param onDismissRequest Déclenché lors de l'annulation ou fermeture.
 * @param onConfirm Déclenché avec les données saisies (titre, montant, catégorie).
 */
@Composable
fun AddTransactionDialog(
    initialTransaction: Transaction? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (title: String, amount: Double, category: Category) -> Unit
) {
    val isEditMode = initialTransaction != null
    var title by remember(initialTransaction) { mutableStateOf(initialTransaction?.title ?: "") }
    var amountText by remember(initialTransaction) {
        mutableStateOf(
            if (initialTransaction != null) {
                if (initialTransaction.amount % 1.0 == 0.0) {
                    initialTransaction.amount.toLong().toString()
                } else {
                    initialTransaction.amount.toString()
                }
            } else ""
        )
    }
    var selectedCategory by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.category ?: Category.ALIMENTATION)
    }
    var isError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val handleDismiss = {
        keyboardController?.hide()
        focusManager.clearFocus()
        onDismissRequest()
    }

    val handleConfirm = {
        val parsedAmount = amountText.toDoubleOrNull()
        if (title.isNotBlank() && parsedAmount != null && parsedAmount > 0) {
            keyboardController?.hide()
            focusManager.clearFocus()
            onConfirm(title.trim(), parsedAmount, selectedCategory)
        } else {
            isError = true
        }
    }

    AlertDialog(
        onDismissRequest = handleDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = DarkDialogBackground,
        modifier = Modifier.border(
            width = 1.5.dp,
            color = DarkDialogOutline,
            shape = RoundedCornerShape(28.dp)
        ),
        title = {
            Text(
                text = if (isEditMode) "Modifier la dépense" else "Nouvelle dépense",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.testTag("dialog_title")
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Saisie du Titre
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (isError) isError = false
                    },
                    label = { Text("Titre de la dépense") },
                    placeholder = { Text("Ex: Courses supermarché", color = DarkTextSecondary) },
                    singleLine = true,
                    isError = isError && title.isBlank(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = VioletPrimaryLight,
                        unfocusedLabelColor = DarkTextSecondary,
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = DarkOutline,
                        focusedContainerColor = DarkDialogFieldBackground,
                        unfocusedContainerColor = DarkDialogFieldBackground,
                        cursorColor = VioletPrimaryLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_transaction_title")
                )

                // Saisie du Montant
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*$"))) {
                            amountText = it
                            if (isError) isError = false
                        }
                    },
                    label = { Text("Montant") },
                    placeholder = { Text("Ex: 5000", color = DarkTextSecondary) },
                    singleLine = true,
                    isError = isError && (amountText.isBlank() || amountText.toDoubleOrNull() == null),
                    trailingIcon = {
                        Text(
                            text = "FCFA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = VioletPrimaryLight,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = VioletPrimaryLight,
                        unfocusedLabelColor = DarkTextSecondary,
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = DarkOutline,
                        focusedContainerColor = DarkDialogFieldBackground,
                        unfocusedContainerColor = DarkDialogFieldBackground,
                        cursorColor = VioletPrimaryLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_transaction_amount")
                )

                // Sélection de Catégorie
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Catégorie",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = DarkTextSecondary
                    )

                    val categories = Category.entries.toTypedArray()
                    val rows = categories.toList().chunked(2)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (category in row) {
                                    val isSelected = category == selectedCategory
                                    val categoryLabel = category.name.lowercase().replaceFirstChar { it.uppercase() }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) VioletPrimary else DarkDialogChipInactive
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) VioletPrimaryLight else DarkOutline,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { selectedCategory = category }
                                            .padding(vertical = 10.dp, horizontal = 8.dp)
                                            .testTag("category_chip_${category.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = category.emoji, fontSize = 14.sp)
                                            Text(
                                                text = categoryLabel,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = handleConfirm,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("btn_confirm_transaction")
            ) {
                Text(
                    text = if (isEditMode) "Enregistrer" else "Ajouter",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = handleDismiss,
                modifier = Modifier.testTag("btn_cancel_transaction")
            ) {
                Text(
                    text = "Annuler",
                    color = DarkTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}