package com.example.househunters.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.data.remote.ListingDetailResponse
import com.example.househunters.data.remote.ListingImageRequest
import com.example.househunters.data.remote.UpsertListingRequest
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.navigation.Screen
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    repository: HouseHuntersRepository,
    token: String?,
    onListingCreated: (ListingDetailResponse) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var imageUrls by rememberSaveable { mutableStateOf(listOf<String>()) }

    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var zip by rememberSaveable { mutableStateOf("") }
    var listingType by rememberSaveable { mutableStateOf("") }
    var pricePerDay by rememberSaveable { mutableStateOf("") }
    var minDays by rememberSaveable { mutableStateOf("") }
    var maxDays by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var pendingImageUrl by rememberSaveable { mutableStateOf("") }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showPropertyTypeMenu by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .padding(bottom = 120.dp)
        ) {
            Text(
                text = "List your home",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Share the basics and attach photo links the backend can store reliably.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (token.isNullOrBlank()) {
                LoggedOutCreateListing(onNavigate = onNavigate)
            } else {
                LabeledField("Location") {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it.take(2).uppercase() },
                            label = { Text("State") },
                            modifier = Modifier.width(110.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = zip,
                        onValueChange = { zip = it.filter(Char::isDigit).take(5) },
                        label = { Text("ZIP code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                LabeledField("Listing details") {
                    ExposedDropdownMenuBox(
                        expanded = showPropertyTypeMenu,
                        onExpandedChange = { showPropertyTypeMenu = !showPropertyTypeMenu }
                    ) {
                        OutlinedTextField(
                            value = listingType.ifBlank { "Select a property type" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Property type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPropertyTypeMenu)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = showPropertyTypeMenu,
                            onDismissRequest = { showPropertyTypeMenu = false }
                        ) {
                            PropertyTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        listingType = option.apiValue
                                        errorMessage = null
                                        showPropertyTypeMenu = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pricePerDay,
                        onValueChange = { pricePerDay = it.filterAllowedDecimal() },
                        label = { Text("Price per day") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = minDays,
                            onValueChange = { minDays = it.filter(Char::isDigit) },
                            label = { Text("Minimum days") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = maxDays,
                            onValueChange = { maxDays = it.filter(Char::isDigit) },
                            label = { Text("Maximum days") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = {
                            Text("What makes this place comfortable, useful, or memorable?")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                LabeledField("Photos") {
                    Text(
                        text = "Paste public image URLs for now. The backend is rejecting embedded camera/file image data with a server error.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = pendingImageUrl,
                            onValueChange = { pendingImageUrl = it.trim() },
                            label = { Text("Image URL") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null)
                            }
                        )
                        Button(
                            onClick = {
                                val normalizedUrl = pendingImageUrl.trim()
                                if (normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")) {
                                    imageUrls = imageUrls + normalizedUrl
                                    pendingImageUrl = ""
                                    errorMessage = null
                                } else {
                                    errorMessage = "Use a full image URL that starts with http:// or https://."
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                    if (imageUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            imageUrls.forEachIndexed { index, imageUrl ->
                                ImageUrlPreviewCard(
                                    imageUrl = imageUrl,
                                    onRemove = {
                                        imageUrls = imageUrls.filterIndexed { imageIndex, _ ->
                                            imageIndex != index
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Example: a direct image link from Unsplash, Cloudinary, S3, or another hosted file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        val validationError = validateListingForm(
                            address = address,
                            city = city,
                            state = state,
                            zip = zip,
                            listingType = listingType,
                            pricePerDay = pricePerDay,
                            minDays = minDays,
                            maxDays = maxDays,
                            description = description,
                            imageUrls = imageUrls
                        )
                        if (validationError != null) {
                            errorMessage = validationError
                            return@Button
                        }

                        val request = UpsertListingRequest(
                            address = address.trim(),
                            city = city.trim(),
                            state = state.trim(),
                            zip = zip.trim(),
                            durationMinDays = minDays.toInt(),
                            durationMaxDays = maxDays.toInt(),
                            pricePerDay = pricePerDay.toDouble(),
                            type = listingType.trim(),
                            description = description.trim(),
                            images = imageUrls.mapIndexed { index, imageUrl ->
                                ListingImageRequest(imageUrl = imageUrl, sortOrder = index)
                            }
                        )

                        scope.launch {
                            isSubmitting = true
                            runCatching {
                                repository.createListing(request, token)
                            }.onSuccess { listing ->
                                onListingCreated(listing)
                            }.onFailure {
                                errorMessage = it.message ?: "Unable to create listing."
                            }
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(if (isSubmitting) "Publishing..." else "Create listing")
                }
            }
        }

        NavBar(
            currentRoute = Screen.CreateListing,
            onNavigate = onNavigate,
            onLogout = onLogout,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun ImageUrlPreviewCard(
    imageUrl: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Listing image preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp
                        )
                    },
                    error = {
                        BrokenImagePreview()
                    }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = imageUrl,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "If this preview looks broken or never appears, double-check the link before publishing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image URL"
                )
            }
        }
    }
}

@Composable
private fun BrokenImagePreview() {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = "Broken image preview",
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Broken",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun LoggedOutCreateListing(
    onNavigate: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Log in to host a home",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You need an account before you can publish a listing and manage bookings.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onNavigate(Screen.Login) }) {
                    Text("Log in")
                }
                Button(onClick = { onNavigate(Screen.Signup) }) {
                    Text("Sign up")
                }
            }
        }
    }
}

@Composable
private fun LabeledField(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

private fun validateListingForm(
    address: String,
    city: String,
    state: String,
    zip: String,
    listingType: String,
    pricePerDay: String,
    minDays: String,
    maxDays: String,
    description: String,
    imageUrls: List<String>
): String? {
    if (address.isBlank() || city.isBlank() || state.isBlank() || zip.isBlank()) {
        return "Fill in the address, city, state, and ZIP code."
    }
    if (listingType.isBlank()) return "Select a property type."
    if (listingType !in PropertyTypeOptions.map(PropertyTypeOption::apiValue)) {
        return "Select one of the supported property types."
    }
    if (description.isBlank()) return "Add a short description for the listing."
    if (state.length != 2) return "Use a 2-letter state abbreviation."
    if (zip.length != 5) return "ZIP code should be 5 digits."
    val parsedPrice = pricePerDay.toDoubleOrNull() ?: return "Enter a valid daily price."
    val parsedMinDays = minDays.toIntOrNull() ?: return "Enter a valid minimum stay."
    val parsedMaxDays = maxDays.toIntOrNull() ?: return "Enter a valid maximum stay."
    if (parsedPrice <= 0.0) return "Price per day must be greater than 0."
    if (parsedMinDays <= 0 || parsedMaxDays <= 0) return "Stay lengths must be greater than 0."
    if (parsedMaxDays < parsedMinDays) return "Maximum stay must be at least the minimum stay."
    if (imageUrls.isEmpty()) return "Add at least one hosted image URL."
    if (imageUrls.any { !it.startsWith("http://") && !it.startsWith("https://") }) {
        return "Every image must use a full http:// or https:// URL."
    }
    return null
}

private data class PropertyTypeOption(
    val apiValue: String,
    val label: String
)

private val PropertyTypeOptions = listOf(
    PropertyTypeOption(apiValue = "Apartment", label = "Apartment"),
    PropertyTypeOption(apiValue = "House", label = "House"),
    PropertyTypeOption(apiValue = "Room", label = "Room"),
    PropertyTypeOption(apiValue = "GuestHouse", label = "Guest House")
)

private fun String.filterAllowedDecimal(): String {
    val filtered = filter { it.isDigit() || it == '.' }
    val firstDecimalIndex = filtered.indexOf('.')
    return if (firstDecimalIndex == -1) {
        filtered
    } else {
        buildString {
            append(filtered.substring(0, firstDecimalIndex + 1))
            append(filtered.substring(firstDecimalIndex + 1).replace(".", ""))
        }
    }
}
