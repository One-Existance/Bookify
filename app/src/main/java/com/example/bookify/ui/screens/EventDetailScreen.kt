package com.example.bookify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bookify.data.DatabaseHelper
import com.example.bookify.data.Event
import com.example.bookify.data.User
import com.example.bookify.ui.theme.*

@Composable
fun EventDetailScreen(
    db: DatabaseHelper,
    event: Event,
    currentUser: User?,
    onBack: () -> Unit,
    onBookingSuccess: () -> Unit
) {

    var showLoginPrompt by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var bookedTicketNo by remember { mutableStateOf("") }
    var alreadyBooked by remember {
        mutableStateOf(currentUser?.let { db.isAlreadyBooked(it.id, event.id) } ?: false)
    }

    val cardBg = parseHexColor(event.cardColorHex)

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Confirm booking", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Book 1 ticket for ${event.title}?\nPrice: ${event.price}",
                    color = TextMuted, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val ticketNo = db.bookEvent(currentUser!!.id, event.id)
                        if (ticketNo != null) {
                            bookedTicketNo = ticketNo
                            alreadyBooked = true
                            showSuccessDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF1A3D2B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
                    }
                    Text("Booked!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Your ticket has been confirmed.", color = TextMuted, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundDark)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(bookedTicketNo, color = Amber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onBookingSuccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("View my tickets") }
                }
            }
        }
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            containerColor = SurfaceDark,
            title = { Text("Login required", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Please log in to book tickets.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = { showLoginPrompt = false; onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Hero banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(cardBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
            Icon(
                Icons.Default.MusicNote,
                null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp).align(Alignment.Center)
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(event.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${event.location} · ${event.date}", color = TextMuted, fontSize = 13.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    modifier = Modifier.weight(1f),
                    icon = "📅",
                    label = "Date",
                    value = event.date
                )
                InfoCard(
                    modifier = Modifier.weight(1f),
                    icon = "🕐",
                    label = "Time",
                    value = event.time
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    modifier = Modifier.weight(1f),
                    icon = "🎟",
                    label = "Slots left",
                    value = "${event.slotsLeft} remaining",
                    valueColor = Amber
                )
                InfoCard(
                    modifier = Modifier.weight(1f),
                    icon = "💰",
                    label = "Price",
                    value = event.price
                )
            }

            // About
            Text("ABOUT", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(event.description, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 22.sp)

            Spacer(Modifier.height(8.dp))
        }

        // Bottom booking bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FavoriteBorder, null, tint = PrimaryPurple, modifier = Modifier.size(22.dp))
                }

                Button(
                    onClick = {
                        if (currentUser == null) showLoginPrompt = true
                        else if (!alreadyBooked) showConfirmDialog = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alreadyBooked) Color(0xFF1A3D2B) else PrimaryPurple
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !alreadyBooked
                ) {
                    Text(
                        if (alreadyBooked) "✓ Already booked" else "Book ticket →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alreadyBooked) Color(0xFF4CAF50) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(icon, fontSize = 14.sp)
                Text(label, color = TextMuted, fontSize = 11.sp)
            }
            Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor("#$hex"))
    } catch (e: Exception) {
        Color(0xFF251F5C)
    }
}
