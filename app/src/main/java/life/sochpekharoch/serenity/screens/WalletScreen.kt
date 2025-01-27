package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.viewmodels.WalletViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WalletScreen(
    navController: NavController,
    viewModel: WalletViewModel = viewModel()
) {
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val balance by viewModel.balance.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wallet",
            style = TextStyle(
                fontFamily = josefinSans,
                fontSize = 24.sp,
                color = Color.Black
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Balance: ₹$balance",
            style = TextStyle(
                fontFamily = josefinSans,
                fontSize = 20.sp,
                color = Color.Black
            )
        )
    }
} 