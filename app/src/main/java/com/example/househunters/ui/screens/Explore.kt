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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.components.TileCard
import com.example.househunters.ui.components.TileItem
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.theme.HouseHuntersTheme

@Composable
fun Explore(
    listings: List<ListingSummaryResponse>,
    favoriteIds: Set<Int>,
    isLoading: Boolean,
    errorMessage: String?,
    currentUserName: String?,
    onRetry: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onOpenListing: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString().trim()
    val filteredListings = remember(listings, query) {
        if (query.isBlank()) {
            listings
        } else {
            listings.filter { listing ->
                listOf(
                    listing.address,
                    listing.city,
                    listing.state,
                    listing.zip,
                    listing.type,
                    listing.description
                ).any { it.contains(query, ignoreCase = true) }
            }
        }
    }
    val suggestions = remember(listings, query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            listings.map { "${it.address}, ${it.city}" }
                .filter { it.contains(query, ignoreCase = true) }
                .take(5)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            SimpleSearchBar(
                textFieldState = textFieldState,
                searchResults = suggestions,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentUserName?.let { "Welcome, $it" } ?: "Explore listings",
                    style = MaterialTheme.typography.titleMedium
                )
                if (errorMessage != null) {
                    Text(
                        text = "Retry",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onRetry)
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

                errorMessage != null && filteredListings.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                filteredListings.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No listings match your search.")
                    }
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
                        items(filteredListings, key = { it.listingId }) { listing ->
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
                    }
                }
            }
        }

        NavBar(
            currentRoute = Screen.Explore,
            onNavigate = onNavigate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchBar(
    textFieldState: TextFieldState,
    searchResults: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search by city, address, or type") }
                )
            },
            expanded = expanded && searchResults.isNotEmpty(),
            onExpandedChange = { expanded = it }
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                searchResults.forEach { result ->
                    ListItem(
                        headlineContent = { Text(result) },
                        modifier = Modifier
                            .clickable {
                                textFieldState.edit { replace(0, length, result) }
                                expanded = false
                            }
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExplorePreview() {
    HouseHuntersTheme {
        Explore(
            listings = emptyList(),
            favoriteIds = emptySet(),
            isLoading = false,
            errorMessage = null,
            currentUserName = "Taylor",
            onRetry = {},
            onToggleFavorite = {},
            onOpenListing = {},
            onNavigate = {}
        )
    }
}
