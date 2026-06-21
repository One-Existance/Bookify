package com.example.bookify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    Column {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = SurfaceDark,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = PrimaryPurple
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun RegisterScreen(
    db: DatabaseHelper,
    onRegisterSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

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

            Spacer(Modifier.height(20.dp))
            Text("Create account", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Join Bookify today", color = TextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(28.dp))

            BookifyTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = "" },
                label = "Full name",
                leadingIcon = { Icon(Icons.Default.Person, null, tint = TextMuted, modifier = Modifier.size(18.dp)) }
            )
            Spacer(Modifier.height(14.dp))

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
            Spacer(Modifier.height(14.dp))

            BookifyTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = "" },
                label = "Phone number",
                leadingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(12.dp))
                        Text("🇹🇿 +255", color = TextMuted, fontSize = 13.sp)
                    }
                },
                keyboardType = KeyboardType.Phone
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = Color(0xFFFF6B6B), fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    when {
                        fullName.isBlank() -> errorMessage = "Please enter your full name"
                        email.isBlank() || !email.contains("@") -> errorMessage = "Please enter a valid email"
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                        phone.isBlank() -> errorMessage = "Please enter your phone number"
                        else -> {
                            val success = db.registerUser(fullName.trim(), email.trim(), password, "+255${phone.trim()}")
                            if (success) {
                                val user = db.loginUser(email.trim(), password)
                                if (user != null) onRegisterSuccess(user)
                            } else {
                                errorMessage = "Email already registered. Try logging in."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Create account →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = TextMuted, fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                    Text("Log in", color = PrimaryPurple, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "By signing up, you agree to our Terms & Privacy Policy",
                color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
