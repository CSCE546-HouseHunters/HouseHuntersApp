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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.househunters.ui.viewmodel.ListingsFilterState
import com.example.househunters.ui.viewmodel.RentalLengthFilter

@Composable
fun Explore(
    listings: List<ListingSummaryResponse>,
    favoriteIds: Set<Int>,
    filters: ListingsFilterState,
    isLoading: Boolean,
    errorMessage: String?,
    currentUserName: String?,
    onSearchQueryChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onMinPriceChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onRentalLengthChange: (RentalLengthFilter) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onOpenListing: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val suggestions = remember(listings, filters.query) {
        if (filters.query.isBlank()) {
            emptyList()
        } else {
            listings.flatMap { listing ->
                listOf(
                    "${listing.address}, ${listing.city}",
                    listing.city,
                    listing.type
                )
            }
                .filter { it.contains(filters.query, ignoreCase = true) }
                .distinct()
                .take(5)
        }
    }
    var showAdvancedFilters by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            SearchListingsBar(
                query = filters.query,
                searchResults = suggestions,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth()
            )

            FiltersSection(
                filters = filters,
                showAdvancedFilters = showAdvancedFilters,
                onToggleAdvancedFilters = { showAdvancedFilters = !showAdvancedFilters },
                onCityChange = onCityChange,
                onStateChange = onStateChange,
                onTypeChange = onTypeChange,
                onMinPriceChange = onMinPriceChange,
                onMaxPriceChange = onMaxPriceChange,
                onRentalLengthChange = onRentalLengthChange,
                onApplyFilters = onApplyFilters,
                onClearFilters = onClearFilters
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

                errorMessage != null && listings.isEmpty() -> {
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

                listings.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No listings match your current filters.")
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
                        items(listings, key = { it.listingId }) { listing ->
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
            onLogout = onLogout,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun FiltersSection(
    filters: ListingsFilterState,
    showAdvancedFilters: Boolean,
    onToggleAdvancedFilters: () -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onMinPriceChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onRentalLengthChange: (RentalLengthFilter) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Stay length",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RentalLengthFilter.entries.forEach { option ->
                FilterChip(
                    selected = filters.rentalLength == option,
                    onClick = { onRentalLengthChange(option) },
                    label = { Text(option.toLabel()) }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Weekend finds show listings that allow short stays. Long-term highlights month-scale rentals.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (showAdvancedFilters) "Hide advanced filters" else "Show advanced filters",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onToggleAdvancedFilters)
        )

        if (showAdvancedFilters) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = filters.city,
                onValueChange = onCityChange,
                label = { Text("City") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = filters.state,
                    onValueChange = onStateChange,
                    label = { Text("State") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = filters.type,
                    onValueChange = onTypeChange,
                    label = { Text("Type") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = filters.minPrice,
                    onValueChange = onMinPriceChange,
                    label = { Text("Min price") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = filters.maxPrice,
                    onValueChange = onMaxPriceChange,
                    label = { Text("Max price") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onApplyFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchListingsBar(
    query: String,
    searchResults: List<String>,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxWidth()
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        onQueryChange(query.trim())
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search address, city, type, or description") }
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
                                onQueryChange(result.trim())
                                expanded = false
                            }
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun RentalLengthFilter.toLabel(): String = when (this) {
    RentalLengthFilter.ALL -> "All"
    RentalLengthFilter.WEEKEND -> "Weekend"
    RentalLengthFilter.LONG_TERM -> "Long-term"
}

@Preview(showBackground = true)
@Composable
fun ExplorePreview() {
    HouseHuntersTheme {
        Explore(
            listings = emptyList(),
            favoriteIds = emptySet(),
            filters = ListingsFilterState(),
            isLoading = false,
            errorMessage = null,
            currentUserName = "Taylor",
            onSearchQueryChange = {},
            onCityChange = {},
            onStateChange = {},
            onTypeChange = {},
            onMinPriceChange = {},
            onMaxPriceChange = {},
            onRentalLengthChange = {},
            onApplyFilters = {},
            onClearFilters = {},
            onRetry = {},
            onToggleFavorite = {},
            onOpenListing = {},
            onNavigate = {},
            onLogout = {}
        )
    }
}
