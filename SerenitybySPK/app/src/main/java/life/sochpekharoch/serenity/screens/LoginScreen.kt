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
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

private val countries = listOf(
    "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan",
    "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina", 
    "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
    "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", 
    "Congo", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic",
    "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
    "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Eswatini", "Ethiopia",
    "Fiji", "Finland", "France",
    "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana",
    "Haiti", "Honduras", "Hungary",
    "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Ivory Coast",
    "Jamaica", "Japan", "Jordan",
    "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan",
    "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg",
    "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", 
    "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar",
    "Namibia", "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria", "North Korea", "North Macedonia", "Norway",
    "Oman",
    "Pakistan", "Palau", "Palestine", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal",
    "Qatar",
    "Romania", "Russia", "Rwanda",
    "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", 
    "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", 
    "Somalia", "South Africa", "South Korea", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Sweden", "Switzerland", "Syria",
    "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", 
    "Tuvalu",
    "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan",
    "Vanuatu", "Vatican City", "Venezuela", "Vietnam",
    "Yemen",
    "Zambia", "Zimbabwe"
)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        Log.d("LoginDebug", "Auth state changed in LoginScreen")
        Log.d("LoginDebug", "isAuthenticated: ${authState.isAuthenticated}")
        Log.d("LoginDebug", "isFirstLogin: ${authState.isFirstLogin}")
        Log.d("LoginDebug", "Full authState: $authState")
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Replace Image with Box for gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF1F1),
                            Color.White
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )

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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Hey There!",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 24.sp,
                            color = Color.Black
                        )
                    )
                    
                    // Email/Password login fields
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("E-mail", fontFamily = abeeZee) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                        shape = RoundedCornerShape(12.dp),
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
                            text = "Forgot password?",
                            color = Color.Gray,
                            fontFamily = abeeZee,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { 
                            if (authState.error?.contains("verify your account") == true) {
                                showVerificationDialog = true
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        enabled = !authState.isLoading
                    ) {
                        Text(
                            text = "Sign In",
                            fontFamily = abeeZee,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Sign up column
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Don't have an account with us?",
                            fontFamily = abeeZee,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        TextButton(
                            onClick = { isSignUpMode = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Create one for free!",
                                fontFamily = abeeZee,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp),
                            color = Color.Black
                        )
                    }

                    authState.error?.let { error ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error,
                                color = Color.Red,
                                fontFamily = abeeZee,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (error.contains("verify your email")) {
                                TextButton(
                                    onClick = { viewModel.resendVerificationEmail() }
                                ) {
                                    Text(
                                        text = "Resend verification email",
                                        color = Color.Black,
                                        fontFamily = abeeZee,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Footer text
        Text(
            text = "An Initiative by SPK Welfare Foundation",
            fontFamily = abeeZee,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (isSignUpMode) {
        Dialog(onDismissRequest = { isSignUpMode = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 24.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Join our community",
                        style = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 14.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("Full Legal Name", fontFamily = abeeZee, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(21.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Black
                        ),
                        textStyle = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 14.sp
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("E-mail ID", fontFamily = abeeZee, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(21.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Black
                        ),
                        textStyle = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 14.sp
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Country Dropdown
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = country,
                            onValueChange = { },
                            placeholder = { Text("Country of Residence", fontFamily = abeeZee, fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isCountryDropdownExpanded = true },
                            shape = RoundedCornerShape(21.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledTextColor = Color.Black
                            ),
                            textStyle = TextStyle(
                                fontFamily = abeeZee,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            readOnly = true,
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Country",
                                    modifier = Modifier.clickable { isCountryDropdownExpanded = true }
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = isCountryDropdownExpanded,
                            onDismissRequest = { isCountryDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.White)
                                .heightIn(max = 250.dp),
                            offset = DpOffset(0.dp, 4.dp)
                        ) {
                            countries.forEach { countryName ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = countryName,
                                            fontFamily = abeeZee,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                    },
                                    onClick = {
                                        country = countryName
                                        isCountryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = { Text("Phone Number (with country code)", fontFamily = abeeZee, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(21.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Black
                        ),
                        textStyle = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 14.sp
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", fontFamily = abeeZee, fontSize = 14.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(21.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Black
                        ),
                        textStyle = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { isSignUpMode = false },
                            modifier = Modifier.weight(1f),
                            enabled = !authState.isLoading
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = abeeZee,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = {
                                if (fullName.isBlank() || email.isBlank() || country.isBlank() || 
                                    phoneNumber.isBlank() || password.isBlank()) {
                                    viewModel.setError("All fields are required")
                                    return@Button
                                }
                                viewModel.signUp(
                                    email = email,
                                    password = password,
                                    fullName = fullName,
                                    country = country,
                                    phoneNumber = phoneNumber
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
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
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
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showVerificationDialog) {
        Dialog(onDismissRequest = { showVerificationDialog = false }) {
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
                        text = "Verify Account",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 24.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Please enter the verification code provided during signup.",
                        style = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 14.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        placeholder = { Text("Verification Code", fontFamily = abeeZee) },
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
                            onClick = { showVerificationDialog = false },
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
                                viewModel.verifyAccount(email, verificationCode)
                                showVerificationDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Verify",
                                fontFamily = abeeZee,
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
                    Text("OK", fontFamily = abeeZee)
                }
            }
        )
    }
} 