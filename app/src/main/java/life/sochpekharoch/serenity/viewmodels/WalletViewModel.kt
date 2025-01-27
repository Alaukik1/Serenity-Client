package life.sochpekharoch.serenity.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WalletViewModel : ViewModel() {
    private val _balance = MutableStateFlow(0)
    val balance: StateFlow<Int> = _balance.asStateFlow()

    fun deductFromWallet(amount: Int, description: String, onComplete: (Boolean) -> Unit) {
        // Implement wallet deduction logic here
        if (_balance.value >= amount) {
            _balance.value -= amount
            onComplete(true)
        } else {
            onComplete(false)
        }
    }
} 