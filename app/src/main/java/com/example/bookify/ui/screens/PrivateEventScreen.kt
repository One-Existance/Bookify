package com.example.bookify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookify.data.DatabaseHelper
import com.example.bookify.data.Event
import com.example.bookify.data.User
import com.example.bookify.ui.theme.*

@Composable
fun PrivateEventScreen(
    db: DatabaseHelper,
    currentUser: User?,
    onEventFound: (Event) -> Unit
) {

    var accessCode by remember { mutableStateOf("") }
    var pasteLink by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val privateBookings = remember(currentUser) {
        currentUser?.let { user ->
            db.getUserBookings(user.id).filter {
                // We identify private ones by checking against private event IDs — just show all for now
                true
            }
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            "Private event",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Lock icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(PrimaryPurple.copy(alpha = 0.2f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, null, tint = PrimaryPurple, modifier = Modifier.size(40.dp))
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Invite-only event",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            "Enter your access code or paste an\ninvite link to join this private event.",
            color = TextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Access code", color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = accessCode,
                onValueChange = { accessCode = it; errorMessage = "" },
                placeholder = { Text("Enter access code...", color = TextMuted, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
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

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceDark)
                Text("or paste link", color = TextMuted, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceDark)
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = pasteLink,
                onValueChange = { pasteLink = it; errorMessage = "" },
                placeholder = { Text("bookify.app/invite/...", color = TextMuted, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Link, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
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

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(errorMessage, color = Color(0xFFFF6B6B), fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val code = when {
                        accessCode.isNotBlank() -> accessCode.trim()
                        pasteLink.isNotBlank() -> pasteLink.substringAfterLast("/").trim()
                        else -> ""
                    }
                    if (code.isEmpty()) {
                        errorMessage = "Enter an access code or paste an invite link"
                    } else {
                        val event = db.getEventByInviteCode(code)
                        if (event != null) onEventFound(event)
                        else errorMessage = "Invalid code. Please check and try again."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Access event →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        Spacer(Modifier.height(28.dp))

        if (privateBookings.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔒", fontSize = 14.sp)
                    Text("My private bookings", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                privateBookings.forEach { booking ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(booking.eventTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                Text("Confirmed · ${booking.eventDate}", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
