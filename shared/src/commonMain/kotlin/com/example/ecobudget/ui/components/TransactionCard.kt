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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecobudget.domain.model.Transaction
import com.example.ecobudget.ui.theme.DarkCardBadge
import com.example.ecobudget.ui.theme.DarkOutline
import com.example.ecobudget.ui.theme.DarkSurfaceVariant
import com.example.ecobudget.ui.theme.DarkTextSecondary
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryName = transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }
    val formattedDate = remember(transaction.date) { formatRelativeDate(transaction.date) }
    val formattedAmount = remember(transaction.amount) { formatAmount(transaction.amount) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = DarkOutline, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("transaction_card_${transaction.id}"),
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardBadge),
                contentAlignment = Alignment.Center
            ) {
                Text(text = transaction.category.emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Éditer",
                        tint = DarkTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "$categoryName • $formattedDate",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = DarkTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "-$formattedAmount",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("delete_transaction_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer ${transaction.title}",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/** Formate le montant de manière portable (ex: 15000 -> "15 000 FCFA") */
private fun formatAmount(amount: Double): String {
    val longAmount = amount.toLong()
    val str = longAmount.toString()
    val result = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        if (count > 0 && count % 3 == 0) {
            result.append(" ")
        }
        result.append(str[i])
        count++
    }
    return "${result.reverse()} FCFA"
}

/** Formate la date relative de façon sécurisée */
private fun formatRelativeDate(timestamp: Long): String {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timeZone).date
    val txDate = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone).date

    return when {
        now == txDate -> "Aujourd'hui"
        now.year == txDate.year && now.month == txDate.month && now.dayOfMonth - txDate.dayOfMonth == 1 -> "Hier"
        else -> "${txDate.dayOfMonth} ${getShortMonthName(txDate.monthNumber)}"
    }
}

private fun getShortMonthName(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> "janv."
        2 -> "févr."
        3 -> "mars"
        4 -> "avr."
        5 -> "mai"
        6 -> "juin"
        7 -> "juil."
        8 -> "août"
        9 -> "sept."
        10 -> "oct."
        11 -> "nov."
        12 -> "déc."
        else -> ""
    }
}