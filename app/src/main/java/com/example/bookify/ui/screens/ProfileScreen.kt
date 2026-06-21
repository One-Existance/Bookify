package com.example.bookify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookify.data.User
import com.example.bookify.ui.theme.*

@Composable
fun ProfileScreen(
    currentUser: User?,
    onLogout: () -> Unit,
    onLoginRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Profile", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

        if (currentUser == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Text("Not logged in", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Log in to view your profile", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onLoginRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Log in", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    currentUser.fullName.split(" ").take(2).joinToString("") { it.first().uppercase() },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(currentUser.fullName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(currentUser.email, color = TextMuted, fontSize = 14.sp)
            Text(currentUser.phone, color = TextMuted, fontSize = 13.sp)

            Spacer(Modifier.height(28.dp))

            ProfileMenuItem(icon = Icons.Default.ConfirmationNumber, label = "My Tickets")
            ProfileMenuItem(icon = Icons.Default.Notifications, label = "Notifications")
            ProfileMenuItem(icon = Icons.Default.Settings, label = "Settings")
            ProfileMenuItem(icon = Icons.Default.Help, label = "Help & Support")

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B6B))
            ) {
                Icon(Icons.Default.Logout, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log out", color = Color(0xFFFF6B6B), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(8.dp))
}
