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
import androidx.compose.ui.tooling.preview.Preview
import com.example.bookify.ui.theme.BookifyTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookify.ui.theme.*

private data class Event(
    val title: String,
    val location: String,
    val date: String,
    val category: String,
    val price: String,
    val cardColor: Color,
    val icon: ImageVector
)

private data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun HomeFeedScreen() {
    val events = listOf(
        Event("Sauti Sol Live in DSM", "Mlimani City Ground", "Jun 20", "Concert", "Tsh 15,000", CardPurple, Icons.Default.Favorite),
        Event("DSM Tech Conference 2025", "Julius Nyerere CC", "Jun 25", "Conference", "Tsh 30,000", CardGreen, Icons.Default.Star)
    )

    val categories = listOf("All", "Concerts", "Sports")
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedNav by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Explore", Icons.Default.Search),
        NavItem("Tickets", Icons.Default.Check),
        NavItem("Profile", Icons.Default.Person)
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    navItems.forEachIndexed { index, item ->
                        val active = index == selectedNav
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedNav = index }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (active) PrimaryPurple else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                item.label,
                                color = if (active) PrimaryPurple else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Good morning,", color = TextMuted, fontSize = 14.sp)
                        Text("Sarah ✨", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SJ", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Search events near you...", color = TextMuted, fontSize = 14.sp)
                }
            }

            item {
                // Category chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    items(categories.size) { i ->
                        val selected = categories[i] == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) PrimaryPurple else ChipBg)
                                .clickable { selectedCategory = categories[i] }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                categories[i],
                                color = if (selected) Color.White else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            item {
                // Section header
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

            items(events.size) { i ->
                EventCard(event = events[i])
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun EventCard(event: Event) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(event.cardColor)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = event.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp)
            )

            Text(event.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HomeFeedScreenPreview() {
    BookifyTheme {
        HomeFeedScreen()
    }
}