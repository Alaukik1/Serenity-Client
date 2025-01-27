package life.sochpekharoch.serenity.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.security.LocalAuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySetupDialog(
    onDismiss: () -> Unit,
    onSetupComplete: () -> Unit,
    localAuthManager: LocalAuthManager
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var useBiometric by remember { mutableStateOf(localAuthManager.isBiometricAvailable()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFF1F1)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Setup Security",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily(Font(R.font.josefin_sans)),
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (localAuthManager.isBiometricAvailable()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Use Biometric",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily(Font(R.font.abeezee_regular)),
                                    color = Color.Black
                                )
                            )
                            Switch(
                                checked = useBiometric,
                                onCheckedChange = { useBiometric = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFE53935)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (!useBiometric) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        color = Color(0xFFE53935),
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily(Font(R.font.abeezee_regular))
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontFamily = FontFamily(Font(R.font.abeezee_regular))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (useBiometric) {
                                localAuthManager.setBiometricEnabled(true)
                                onSetupComplete()
                            } else {
                                if (password.length < 4) {
                                    error = "Password must be at least 4 characters"
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    error = "Passwords don't match"
                                    return@Button
                                }
                                localAuthManager.setLocalPassword(password)
                                onSetupComplete()
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Setup",
                            fontFamily = FontFamily(Font(R.font.abeezee_regular))
                        )
                    }
                }
            }
        }
    }
} 