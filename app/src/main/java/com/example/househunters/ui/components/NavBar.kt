package com.example.househunters.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.theme.HouseHuntersTheme

@Composable
fun NavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(64.dp), // Matched shape for better shadow clipping
                ambientColor = Color.Black.copy(alpha = 0.1f), // Softened shadow as suggested
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .fillMaxWidth(),
        shape = RoundedCornerShape(64.dp),
        color = Color.White,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Search,
                label = "Explore",
                isSelected = currentRoute == Screen.Explore,
                onClick = { onNavigate(Screen.Explore) }
            )
            NavItem(
                icon = Icons.Default.PersonOutline,
                label = "My Stuff",
                isSelected = currentRoute == Screen.Saved,
                onClick = { onNavigate(Screen.Saved) }
            )
            NavItem(
                icon = Icons.Default.AddCircleOutline,
                label = "Host",
                isSelected = currentRoute == Screen.CreateListing,
                onClick = { onNavigate(Screen.CreateListing) }
            )
            if (onLogout != null) {
                Box {
                    MoreMenuItem(
                        isSelected = currentRoute == Screen.Profile,
                        onOpenMenu = { showMoreMenu = true }
                    )
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                showMoreMenu = false
                                onNavigate(Screen.Profile)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMoreMenu = false
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(60.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF202020) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MoreMenuItem(
    isSelected: Boolean,
    onOpenMenu: () -> Unit
) {
    IconButton(
        onClick = onOpenMenu,
        modifier = Modifier.size(60.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = if (isSelected) Color(0xFF202020) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    HouseHuntersTheme {
        Column {
            Spacer(modifier = Modifier.weight(1f))
            NavBar(
                currentRoute = Screen.Explore,
                onNavigate = {},
                onLogout = {}
            )
        }
    }
}
