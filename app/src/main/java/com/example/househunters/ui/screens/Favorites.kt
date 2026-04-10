package com.example.househunters.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.components.TileCard
import com.example.househunters.ui.components.TileItem
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.theme.HouseHuntersTheme

@Composable
fun Favorites(onNavigate: (String) -> Unit) {
    val tileItems = listOf(
        TileItem("1", "Modern Villa", android.R.drawable.ic_menu_gallery, false),
        TileItem("2", "Cozy Apartment", android.R.drawable.ic_menu_gallery, true),
        TileItem("3", "Luxury House", android.R.drawable.ic_menu_gallery, false),
        TileItem("4", "Beachfront Condo", android.R.drawable.ic_menu_gallery, false)
    )

    // Filter to only show liked items
    val favoriteItems = tileItems.filter { it.isLiked }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                )

            Spacer(modifier = Modifier.height(20.dp))

            if (favoriteItems.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "No favorites yet", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 100.dp)
                ) {
                    items(favoriteItems) { item ->
                        TileCard(
                            item = item,
                            onClick = { onNavigate(Screen.Listing) },
                            onLikeClick = { /* Handle like state change if needed */ }
                        )
                    }
                }
            }
        }

        NavBar(
            currentRoute = Screen.Favorites,
            onNavigate = onNavigate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesPreview() {
    HouseHuntersTheme {
        Favorites(onNavigate = {})
    }
}


