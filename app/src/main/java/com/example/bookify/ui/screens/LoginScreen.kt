package com.example.bookify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookify.data.DatabaseHelper
import com.example.bookify.data.User
import com.example.bookify.ui.theme.*

@Composable
fun BookifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted, fontSize = 13.sp) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = SurfaceDark,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PrimaryPurple,
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun LoginScreen(
    db: DatabaseHelper?,
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryPurple.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurple)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("BOOKIFY", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Book Events Anywhere In Tanzania", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(36.dp))

            BookifyTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = "Email address",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(14.dp))

            BookifyTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = "Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = TextMuted, modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = Color(0xFFFF6B6B), fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() -> errorMessage = "Please enter your email"
                        password.isBlank() -> errorMessage = "Please enter your password"
                        else -> {
                            isLoading = true
                            val user = db?.loginUser(email.trim(), password)
                            isLoading = false
                            if (user != null) onLoginSuccess(user)
                            else errorMessage = "Invalid email or password"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Log in", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", color = TextMuted, fontSize = 14.sp)
                TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign up", color = PrimaryPurple, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0C1F, widthDp = 393, heightDp = 851)
@Composable
fun LoginScreenPreview() {
    BookifyTheme {
        LoginScreen(
            db = null,
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}
