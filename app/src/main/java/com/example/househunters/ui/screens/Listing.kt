package com.example.househunters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.data.remote.ListingDetailResponse
import com.example.househunters.ui.components.NavBar

@Composable
fun ListingRoute(
    listingId: Int,
    repository: HouseHuntersRepository,
    token: String?,
    favoriteIds: Set<Int>,
    onBackClick: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    var listing by remember(listingId) { mutableStateOf<ListingDetailResponse?>(null) }
    var errorMessage by remember(listingId) { mutableStateOf<String?>(null) }
    var isLoading by remember(listingId) { mutableStateOf(true) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var bookingMessage by remember { mutableStateOf<String?>(null) }
    var isBooking by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        isLoading = true
        errorMessage = null
        bookingMessage = null
        runCatching {
            repository.getListingDetails(listingId)
        }.onSuccess {
            listing = it
        }.onFailure {
            errorMessage = it.message ?: "Unable to load listing."
        }
        isLoading = false
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        listing == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage ?: "Listing not found.")
            }
        }

        else -> {
            val currentListing = listing ?: return
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        LazyRow(modifier = Modifier.fillMaxSize()) {
                            items(currentListing.images) { image ->
                                AsyncImage(
                                    model = image.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .height(300.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.8f)
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.8f)
                        ) {
                            IconButton(onClick = { onToggleFavorite(currentListing.listingId) }) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorite",
                                    tint = if (favoriteIds.contains(currentListing.listingId)) Color.Red else Color.Gray
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${currentListing.address}, ${currentListing.city}, ${currentListing.state} ${currentListing.zip}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = currentListing.type,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "$${currentListing.pricePerDay}/day",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Hosted by ${currentListing.ownerName}",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Stay length: ${currentListing.durationMinDays}-${currentListing.durationMaxDays} days",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = currentListing.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Book this stay",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AuthTextField(
                            value = startDate,
                            placeholder = "Start date (YYYY-MM-DD)",
                            onValueChange = { startDate = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AuthTextField(
                            value = endDate,
                            placeholder = "End date (YYYY-MM-DD)",
                            onValueChange = { endDate = it }
                        )
                        if (bookingMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = bookingMessage ?: "",
                                color = if (bookingMessage?.startsWith("Booking created") == true) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        AuthPrimaryButton(
                            label = if (isBooking) "Submitting..." else "Reserve",
                            onClick = {
                                if (token.isNullOrBlank()) {
                                    bookingMessage = "Log in before creating a booking."
                                } else if (startDate.isBlank() || endDate.isBlank()) {
                                    bookingMessage = "Enter both dates in YYYY-MM-DD format."
                                } else {
                                    isBooking = true
                                    bookingMessage = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isBooking && !token.isNullOrBlank() && startDate.isNotBlank() && endDate.isNotBlank()) {
                            LaunchedEffect(isBooking) {
                                runCatching {
                                    repository.createBooking(
                                        listingId = currentListing.listingId,
                                        startDate = startDate,
                                        endDate = endDate,
                                        token = token
                                    )
                                }.onSuccess { booking ->
                                    bookingMessage = "Booking created: ${booking.status}"
                                }.onFailure {
                                    bookingMessage = it.message ?: "Unable to create booking."
                                }
                                isBooking = false
                            }
                        }

                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NavBar(
                        currentRoute = null,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}
