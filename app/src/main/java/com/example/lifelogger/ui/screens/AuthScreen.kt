package com.example.lifelogger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifelogger.ui.AuthUiState
import com.example.lifelogger.ui.AuthViewModel
import com.example.lifelogger.ui.components.LightGrayBg
import com.example.lifelogger.ui.components.TealGreen
import com.example.lifelogger.ui.components.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit
) {
    // UI state to toggle between Login and Register
    var isRegisterMode by remember { mutableStateOf(false) }
    
    // Form inputs
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = LightGrayBg) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text("Life Logger", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = TealGreen)
            Text(
                text = if (isRegisterMode) "Create your account" else "Welcome back!",
                fontSize = 16.sp, 
                color = TextDark.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // 1. NAME FIELD (Only visible in Register Mode)
            AnimatedVisibility(visible = isRegisterMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealGreen) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // 2. EMAIL FIELD
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TealGreen) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. PASSWORD FIELD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealGreen) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TealGreen
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // 4. FEEDBACK MESSAGES (Error/Success)
            Spacer(modifier = Modifier.height(16.dp))
            when (uiState) {
                is AuthUiState.Loading -> CircularProgressIndicator(color = TealGreen, modifier = Modifier.size(24.dp))
                is AuthUiState.Error -> Text((uiState as AuthUiState.Error).message, color = Color.Red, fontSize = 14.sp)
                is AuthUiState.Success -> Text((uiState as AuthUiState.Success).message, color = TealGreen, fontSize = 14.sp)
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. MAIN ACTION BUTTON
            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.registerUser(name, email, password, onSuccess = onNavigateToHome)
                    } else {
                        viewModel.loginUser(email, password, onSuccess = onNavigateToHome)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealGreen),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text(if (isRegisterMode) "Create Account" else "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. TOGGLE MODE LINK
            TextButton(onClick = { 
                isRegisterMode = !isRegisterMode 
                viewModel.resetState()
            }) {
                Text(
                    text = if (isRegisterMode) "Already have an account? Login" else "New here? Create an account",
                    color = TealGreen
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. OFFLINE BYPASS
            TextButton(onClick = onNavigateToHome) {
                Text("Skip & Use Offline", color = TextDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}