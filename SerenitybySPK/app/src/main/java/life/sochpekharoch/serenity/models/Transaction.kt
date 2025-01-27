package life.sochpekharoch.serenity.models

import java.util.*

enum class TransactionType {
    CREDIT,
    DEBIT
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val timestamp: Date = Date()
) 