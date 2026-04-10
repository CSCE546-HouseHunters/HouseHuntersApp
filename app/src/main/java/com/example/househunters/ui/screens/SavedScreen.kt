package com.example.househunters.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.components.TileCard
import com.example.househunters.ui.components.TileItem
import com.example.househunters.ui.navigation.Screen

@Composable
fun SavedScreen(
    listings: List<ListingSummaryResponse>,
    favoriteIds: Set<Int>,
    isLoading: Boolean,
    errorMessage: String?,
    currentUserName: String?,
    isLoggedIn: Boolean,
    onRetry: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onOpenListing: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val savedListings = listings.filter { listing -> favoriteIds.contains(listing.listingId) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUserName?.let { "$it's saved homes" } ?: "Saved listings",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isLoggedIn) {
                            "Keep track of the listings you want to revisit."
                        } else {
                            "Log in to save homes and see them collected here."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (errorMessage != null) {
                    Text(
                        text = "Retry",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable(onClick = onRetry)
                    )
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null && savedListings.isEmpty() -> {
                    SavedStateMessage(
                        title = "We couldn't load your saved homes",
                        body = errorMessage
                    )
                }

                !isLoggedIn -> {
                    SavedStateMessage(
                        title = "No saved homes yet",
                        body = "Sign in from the welcome flow, then tap the heart on any listing to build your saved collection."
                    )
                }

                savedListings.isEmpty() -> {
                    SavedStateMessage(
                        title = "Nothing favorited yet",
                        body = "Tap the heart on a listing in Explore and it will show up here."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 100.dp
                        )
                    ) {
                        items(savedListings, key = { it.listingId }) { listing ->
                            val cardItem = TileItem(
                                id = listing.listingId,
                                title = "${listing.address}, ${listing.city}",
                                subtitle = listing.type,
                                imageUrl = listing.images.firstOrNull()?.imageUrl,
                                priceLabel = "$${listing.pricePerDay}/day",
                                isLiked = true
                            )
                            TileCard(
                                item = cardItem,
                                onClick = { onOpenListing(listing.listingId) },
                                onLikeClick = { onToggleFavorite(listing.listingId) }
                            )
                        }
                    }
                }
            }
        }

        NavBar(
            currentRoute = Screen.Saved,
            onNavigate = onNavigate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun SavedStateMessage(
    title: String,
    body: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
