package com.example.bookify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookify.data.Event
import com.example.bookify.data.User
import com.example.bookify.ui.theme.*

private fun parseHexColor(hex: String): Color {
    return try {
        val value = hex.trimStart('#').toLong(16) or 0xFF000000.toLong()
        Color(value)
    } catch (e: Exception) {
        Color(0xFF251F5C)
    }
}

@Composable
fun HomeFeedScreen(
    events: List<Event>,
    currentUser: User? = null
) {
    val allCategories = listOf("All") + events.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf("All") }

    val displayed = if (selectedCategory == "All") events
    else events.filter { it.category == selectedCategory }

    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        else -> "Good evening,"
    }
    val displayName = currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Guest"

    Scaffold(
        containerColor = BackgroundDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(greeting, color = TextMuted, fontSize = 14.sp)
                        Text("$displayName ✨", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Search events near you...", color = TextMuted, fontSize = 14.sp)
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    items(allCategories.size) { i ->
                        val selected = allCategories[i] == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) PrimaryPurple else ChipBg)
                                .clickable { selectedCategory = allCategories[i] }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                allCategories[i],
                                color = if (selected) Color.White else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nearby events", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("See all →", color = TextMuted, fontSize = 13.sp)
                }
                Spacer(Modifier.height(14.dp))
            }

            if (displayed.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No events in this category", color = TextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                items(displayed.size) { i ->
                    EventFeedCard(event = displayed[i])
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun EventFeedCard(event: Event) {
    val cardBg = parseHexColor(event.cardColorHex)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp)
            )

            Text(event.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${event.location} · ${event.date}", color = TextMuted, fontSize = 13.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(event.category, color = Color.White, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Amber)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(event.price, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private val previewEvents = listOf(
    Event(1, "Sauti Sol Live in DSM", "Mlimani City Ground", "Jun 20, 2025", "7:00 PM", "Concert", "Tsh 15,000", 48, "", false, null, "251F5C"),
    Event(2, "DSM Tech Conference 2025", "Julius Nyerere CC", "Jun 25, 2025", "9:00 AM", "Conference", "Tsh 30,000", 120, "", false, null, "1A3D2B")
)

@Preview(showBackground = true, backgroundColor = 0xFF0F0C1F, widthDp = 393, heightDp = 851)
@Composable
fun HomeFeedScreenPreview() {
    BookifyTheme {
        HomeFeedScreen(
            events = previewEvents,
            currentUser = User(1, "Sarah Mahwera", "sarah@example.com", "+255700000000")
        )
    }
}
