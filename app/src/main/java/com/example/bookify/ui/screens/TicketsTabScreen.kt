package com.example.bookify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.example.bookify.data.DatabaseHelper
import com.example.bookify.data.Event
import com.example.bookify.data.User
import com.example.bookify.ui.theme.*

private data class NavItemT(val label: String, val icon: ImageVector)

@Composable
fun TicketsTabScreen(
    db: DatabaseHelper,
    currentUser: User?,
    selectedNav: Int,
    onNavSelected: (Int) -> Unit,
    onEventFound: (Event) -> Unit
) {
    var innerTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItemT("Home", Icons.Default.Home),
        NavItemT("Explore", Icons.Default.Search),
        NavItemT("Tickets", Icons.Default.ConfirmationNumber),
        NavItemT("Profile", Icons.Default.Person)
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
                        val interactionSource = remember { MutableInteractionSource() }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = false)
                            ) { onNavSelected(index) }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (active) PrimaryPurple else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(item.label, color = if (active) PrimaryPurple else TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("My Tickets", "Private").forEachIndexed { index, label ->
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (innerTab == index) PrimaryPurple else Color.Transparent)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { innerTab = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            color = if (innerTab == index) Color.White else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = if (innerTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            when (innerTab) {
                0 -> MyTicketsScreen(db = db, currentUser = currentUser)
                1 -> PrivateEventScreen(
                    db = db,
                    currentUser = currentUser,
                    onEventFound = onEventFound
                )
            }
        }
    }
}
