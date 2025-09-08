package com.ballabotond.trackit.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.ui.theme.FeatherIcon
import com.ballabotond.trackit.ui.viewmodel.AuthUiState
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    val passwordsMatch = password == confirmPassword
    val isFormValid = username.isNotBlank() && 
                     email.isNotBlank() && 
                     password.isNotBlank() && 
                     confirmPassword.isNotBlank() && 
                     passwordsMatch &&
                     email.contains("@")
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "TrackIt",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Create your account to get started.",
                color = Color(0xFFAAAAAA),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color(0xFFAAAAAA)) },
                leadingIcon = {
                    FeatherIcon(
                        icon = FeatherIconsCollection.User,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color(0xFFAAAAAA)) },
                leadingIcon = {
                    FeatherIcon(
                        icon = FeatherIconsCollection.Mail,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color(0xFFAAAAAA)) },
                leadingIcon = {
                    FeatherIcon(
                        icon = FeatherIconsCollection.Lock,
                        tint = Color(0xFF4CAF50),
                        size = 20.dp
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        FeatherIcon(
                            icon = if (showPassword) FeatherIconsCollection.EyeOff else FeatherIconsCollection.Eye,
                            tint = Color(0xFFAAAAAA),
                            size = 20.dp
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = Color(0xFFAAAAAA)) },
                leadingIcon = {
                    FeatherIcon(
                        icon = FeatherIconsCollection.Lock,
                        tint = Color(0xFF4CAF50),
                        size = 20.dp
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        FeatherIcon(
                            icon = if (showConfirmPassword) FeatherIconsCollection.EyeOff else FeatherIconsCollection.Eye,
                            tint = Color(0xFFAAAAAA),
                            size = 20.dp
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passwordsMatch || confirmPassword.isEmpty()) Color(0xFF4CAF50) else Color.Red,
                    unfocusedBorderColor = if (passwordsMatch || confirmPassword.isEmpty()) Color(0xFF333333) else Color.Red,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )
            
            // Password match indicator
            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Error Message
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Register Button
            Button(
                onClick = {
                    if (isFormValid) {
                        onRegister(username.trim(), password, email.trim())
                    }
                },
                enabled = !uiState.isLoading && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF333333),
                    disabledContentColor = Color(0xFF666666)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign In",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
