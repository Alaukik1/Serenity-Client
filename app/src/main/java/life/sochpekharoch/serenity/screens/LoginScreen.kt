package life.sochpekharoch.serenity.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.viewmodels.AuthViewModel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.navigation.NavController
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import life.sochpekharoch.serenity.components.AnimatedGradientBackground

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))

    LaunchedEffect(authState) {
        if (authState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gradient background
        AnimatedGradientBackground {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                
                // Logo
                Image(
                    painter = painterResource(R.drawable.serenity_logo),
                    contentDescription = "Serenity Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Serenity",
                    style = TextStyle(
                        fontFamily = josefinSans,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!isSignUpMode) {
                            // Login Mode
                            Text(
                                text = "Welcome Back!",
                                style = TextStyle(
                                    fontFamily = josefinSans,
                                    fontSize = 24.sp,
                                    color = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("E-mail", fontFamily = abeeZee) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("Password", fontFamily = abeeZee) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Black
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            )

                            TextButton(
                                onClick = { showResetDialog = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    fontFamily = abeeZee,
                                    color = Color.Gray
                                )
                            }

                            Button(
                                onClick = { viewModel.login(email, password) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black
                                ),
                                shape = RoundedCornerShape(20.dp),
                                enabled = !authState.isLoading
                            ) {
                                if (authState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "Sign In",
                                        fontFamily = abeeZee,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Don't have an account with us?",
                                    fontFamily = abeeZee,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                OutlinedButton(
                                    onClick = { isSignUpMode = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Black
                                    ),
                                    border = BorderStroke(1.dp, Color.Black),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "Create Account",
                                        fontFamily = abeeZee,
                                        color = Color.Black,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        } else {
                            // Sign Up Mode
                            Text(
                                text = "Create Account",
                                style = TextStyle(
                                    fontFamily = josefinSans,
                                    fontSize = 24.sp,
                                    color = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = { Text("Full Name", fontFamily = abeeZee) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("E-mail", fontFamily = abeeZee) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("Password", fontFamily = abeeZee) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Black
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = { Text("Confirm Password", fontFamily = abeeZee) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Black
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    onClick = { isSignUpMode = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Cancel",
                                        fontFamily = abeeZee,
                                        color = Color.Gray
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                            viewModel.setError("All fields are required")
                                            return@Button
                                        }
                                        if (password != confirmPassword) {
                                            viewModel.setError("Passwords do not match")
                                            return@Button
                                        }
                                        
                                        viewModel.signUp(
                                            email = email,
                                            password = password,
                                            phoneNumber = ""
                                        )
                                        showSuccessDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Sign Up",
                                        fontFamily = abeeZee,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (authState.error != null) {
                            Text(
                                text = authState.error!!,
                                color = Color.Red,
                                fontFamily = abeeZee,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Footer text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.spk_logo),
                contentDescription = "SPK Logo",
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 4.dp)
            )
            
            Text(
                text = "SPK Welfare Foundation",
                fontFamily = abeeZee,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }

    if (showResetDialog) {
        Dialog(onDismissRequest = { showResetDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reset Password",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 24.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Enter your email address and we'll send you instructions to reset your password.",
                        style = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 14.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("E-mail", fontFamily = abeeZee) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(21.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { showResetDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = abeeZee,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.sendPasswordResetEmail(email)
                                showResetDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Send",
                                fontFamily = abeeZee,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.clearState()
                isSignUpMode = false
                showSuccessDialog = false
            },
            title = {
                Text(
                    text = "Account Created Successfully",
                    style = TextStyle(
                        fontFamily = josefinSans,
                        fontSize = 20.sp
                    )
                )
            },
            text = {
                Text(
                    text = "Please check your email for verification instructions.",
                    style = TextStyle(
                        fontFamily = abeeZee,
                        fontSize = 14.sp
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.clearState()
                        isSignUpMode = false
                        showSuccessDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text(
                        text = "OK",
                        fontFamily = abeeZee,
                        color = Color.White
                    )
                }
            }
        )
    }
} 