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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.househunters.data.remote.BookingResponse
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.components.TileCard
import com.example.househunters.ui.components.TileItem
import com.example.househunters.ui.navigation.Screen

@Composable
fun SavedScreen(
    listings: List<ListingSummaryResponse>,
    myListings: List<ListingSummaryResponse>,
    bookings: List<BookingResponse>,
    favoriteIds: Set<Int>,
    isLoading: Boolean,
    errorMessage: String?,
    currentUserName: String?,
    isLoggedIn: Boolean,
    onRetry: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onOpenListing: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
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
                        text = currentUserName?.let { "$it's dashboard" } ?: "My stuff",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isLoggedIn) {
                            "Review your saved homes, your own listings, and the trips you have booked."
                        } else {
                            "Log in to see your saved homes, bookings, and hosted listings."
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

                errorMessage != null && listings.isEmpty() && myListings.isEmpty() && bookings.isEmpty() -> {
                    MyStuffEmptyState(
                        title = "We couldn't load your dashboard",
                        body = errorMessage
                    )
                }

                !isLoggedIn -> {
                    MyStuffEmptyState(
                        title = "Nothing to show yet",
                        body = "Sign in first, then this screen will collect your saved homes, your listings, and your bookings."
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
                        item {
                            SectionHeader(
                                title = "My listings",
                                subtitle = "Homes you published as a host"
                            )
                        }
                        if (myListings.isEmpty()) {
                            item {
                                SectionMessage("You have not created any listings yet. Use Host to publish one.")
                            }
                        } else {
                            items(myListings, key = { it.listingId }) { listing ->
                                ListingCardRow(
                                    listing = listing,
                                    favoriteIds = favoriteIds,
                                    onToggleFavorite = onToggleFavorite,
                                    onOpenListing = onOpenListing
                                )
                            }
                        }

                        item {
                            SectionHeader(
                                title = "My bookings",
                                subtitle = "Trips you booked as a renter"
                            )
                        }
                        if (bookings.isEmpty()) {
                            item {
                                SectionMessage("You do not have any bookings yet.")
                            }
                        } else {
                            items(bookings, key = { it.bookingId }) { booking ->
                                BookingCard(booking = booking)
                            }
                        }

                        item {
                            SectionHeader(
                                title = "Saved homes",
                                subtitle = "Favorited listings you want to revisit"
                            )
                        }
                        if (listings.isEmpty()) {
                            item {
                                SectionMessage("You have not favorited any homes yet.")
                            }
                        } else {
                            items(listings, key = { it.listingId }) { listing ->
                                ListingCardRow(
                                    listing = listing,
                                    favoriteIds = favoriteIds,
                                    onToggleFavorite = onToggleFavorite,
                                    onOpenListing = onOpenListing
                                )
                            }
                        }
                    }
                }
            }
        }

        NavBar(
            currentRoute = Screen.Saved,
            onNavigate = onNavigate,
            onLogout = onLogout,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun ListingCardRow(
    listing: ListingSummaryResponse,
    favoriteIds: Set<Int>,
    onToggleFavorite: (Int) -> Unit,
    onOpenListing: (Int) -> Unit
) {
    val cardItem = TileItem(
        id = listing.listingId,
        title = "${listing.address}, ${listing.city}",
        subtitle = listing.type,
        imageUrl = listing.images.firstOrNull()?.imageUrl,
        priceLabel = "$${listing.pricePerDay}/day",
        isLiked = favoriteIds.contains(listing.listingId)
    )
    TileCard(
        item = cardItem,
        onClick = { onOpenListing(listing.listingId) },
        onLikeClick = { onToggleFavorite(listing.listingId) }
    )
}

@Composable
private fun BookingCard(
    booking: BookingResponse
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Booking #${booking.bookingId}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${booking.startDate} to ${booking.endDate}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${booking.status}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Listing ID ${booking.listingId}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun MyStuffEmptyState(
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
