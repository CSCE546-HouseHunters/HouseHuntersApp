package com.example.househunters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.data.remote.BookingResponse
import com.example.househunters.data.remote.ListingAvailabilityRangeResponse
import com.example.househunters.ui.components.NavBar
import com.example.househunters.ui.viewmodel.ListingUiState
import com.example.househunters.ui.viewmodel.ListingViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ListingRoute(
    listingId: Int,
    repository: HouseHuntersRepository,
    token: String?,
    currentUserId: Int?,
    favoriteIds: Set<Int>,
    onBackClick: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onListingDeleted: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: ListingViewModel = viewModel(
        key = "listing-$listingId",
        factory = ListingViewModel.factory(
            listingId = listingId,
            repository = repository,
            token = token,
            currentUserId = currentUserId
        )
    )
    val uiState = viewModel.uiState

    var startDate by rememberSaveable(listingId) { mutableStateOf("") }
    var endDate by rememberSaveable(listingId) { mutableStateOf("") }
    var showStartDatePicker by rememberSaveable(listingId) { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable(listingId) { mutableStateOf(false) }

    LaunchedEffect(uiState.bookingMessage) {
        if (uiState.bookingMessage?.startsWith("Booking created") == true) {
            startDate = ""
            endDate = ""
        }
    }

    // Turn unavailable ranges into individual date strings so validation stays simple.
    val blockedDates = remember(uiState.availabilityRanges) {
        uiState.availabilityRanges.toBlockedDateSet()
    }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.listing == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.errorMessage ?: "Listing not found.")
            }
        }

        else -> {
            val currentListing = uiState.listing ?: return
            val isOwner = currentUserId == currentListing.userId
            // Keep the reserve button disabled until the date selection is safe to submit.
            val guestValidationMessage = guestBookingValidationMessage(
                token = token,
                uiState = uiState,
                startDate = startDate,
                endDate = endDate,
                blockedDates = blockedDates
            )

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

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .zIndex(1f)
                                .padding(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.88f)
                            ) {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.88f)
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
                        if (isOwner) {
                            OwnerManagementSection(
                                uiState = uiState,
                                onDeleteListing = { viewModel.deleteListing(onListingDeleted) },
                                onUpdateBookingStatus = viewModel::updateOwnerBookingStatus
                            )
                        } else {
                            GuestBookingSection(
                                startDate = startDate,
                                endDate = endDate,
                                bookingMessage = uiState.bookingMessage,
                                validationMessage = guestValidationMessage,
                                isBooking = uiState.isBooking,
                                onStartDateClick = { showStartDatePicker = true },
                                onEndDateClick = { showEndDatePicker = true },
                                onReserve = { viewModel.createBooking(startDate, endDate) }
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "Unavailable dates",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        when {
                            uiState.availabilityLoading -> {
                                CircularProgressIndicator()
                            }

                            uiState.availabilityError != null -> {
                                Text(
                                    text = uiState.availabilityError ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            uiState.availabilityRanges.isEmpty() -> {
                                Text(
                                    text = "No unavailable dates right now.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            else -> {
                                uiState.availabilityRanges.forEach { range ->
                                    AvailabilityRangeCard(range = range)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
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
                        onNavigate = onNavigate,
                        onLogout = onLogout
                    )
                }
            }
        }
    }

    if (showStartDatePicker) {
        BookingDatePickerDialog(
            initialDate = startDate,
            blockedDates = blockedDates,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { selectedDate ->
                startDate = selectedDate
                if (endDate.isNotBlank() && endDate < selectedDate) {
                    endDate = ""
                }
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        BookingDatePickerDialog(
            initialDate = endDate.ifBlank { startDate },
            blockedDates = blockedDates,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { selectedDate ->
                endDate = selectedDate
                showEndDatePicker = false
            }
        )
    }
}

@Composable
private fun GuestBookingSection(
    startDate: String,
    endDate: String,
    bookingMessage: String?,
    validationMessage: String?,
    isBooking: Boolean,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onReserve: () -> Unit
) {
    Text(
        text = "Book this stay",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(12.dp))
    BookingDateField(
        label = "Start date",
        value = startDate,
        onClick = onStartDateClick
    )
    Spacer(modifier = Modifier.height(12.dp))
    BookingDateField(
        label = "End date",
        value = endDate,
        onClick = onEndDateClick
    )
    if (bookingMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = bookingMessage,
            color = if (bookingMessage.startsWith("Booking created")) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    } else if (!validationMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = validationMessage,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    AuthPrimaryButton(
        label = if (isBooking) "Submitting..." else "Reserve",
        onClick = onReserve,
        enabled = !isBooking && validationMessage == null,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun OwnerManagementSection(
    uiState: ListingUiState,
    onDeleteListing: () -> Unit,
    onUpdateBookingStatus: (Int, String) -> Unit
) {
    Text(
        text = "Manage this listing",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "This home belongs to you. You can review booking requests here and remove the listing if needed.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (uiState.bookingMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = uiState.bookingMessage ?: "",
            color = if (uiState.bookingMessage?.contains("updated to") == true) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }

    Spacer(modifier = Modifier.height(18.dp))
    Text(
        text = "Booking requests",
        style = MaterialTheme.typography.titleSmall
    )
    Spacer(modifier = Modifier.height(10.dp))
    when {
        uiState.ownerBookingsLoading -> CircularProgressIndicator()

        uiState.ownerBookingsError != null -> Text(
            text = uiState.ownerBookingsError ?: "",
            color = MaterialTheme.colorScheme.error
        )

        uiState.ownerBookings.isEmpty() -> Text(
            text = "No booking requests have been submitted yet.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        else -> {
            uiState.ownerBookings.forEach { booking ->
                OwnerBookingCard(
                    booking = booking,
                    isUpdating = uiState.activeBookingStatusId == booking.bookingId,
                    onUpdateBookingStatus = onUpdateBookingStatus
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    AuthPrimaryButton(
        label = if (uiState.isDeleting) "Deleting..." else "Delete listing",
        onClick = onDeleteListing,
        enabled = !uiState.isDeleting,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun guestBookingValidationMessage(
    token: String?,
    uiState: ListingUiState,
    startDate: String,
    endDate: String,
    blockedDates: Set<String>
): String? = when {
    token.isNullOrBlank() -> "Log in before creating a booking."
    uiState.availabilityLoading -> "Wait for unavailable dates to load."
    uiState.availabilityError != null -> uiState.availabilityError
    startDate.isBlank() || endDate.isBlank() -> "Select both dates before booking."
    endDate < startDate -> "End date must be on or after the start date."
    hasBlockedDateInRange(startDate, endDate, blockedDates) ->
        "Those dates overlap with unavailable dates."

    else -> null
}

@Composable
private fun OwnerBookingCard(
    booking: BookingResponse,
    isUpdating: Boolean,
    onUpdateBookingStatus: (Int, String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Booking #${booking.bookingId}",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "${booking.startDate} to ${booking.endDate}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${booking.status}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (booking.status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onUpdateBookingStatus(booking.bookingId, "Confirmed") },
                        enabled = !isUpdating,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isUpdating) "Working..." else "Accept")
                    }
                    OutlinedButton(
                        onClick = { onUpdateBookingStatus(booking.bookingId, "Rejected") },
                        enabled = !isUpdating,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isUpdating) "Working..." else "Deny")
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityRangeCard(range: ListingAvailabilityRangeResponse) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Unavailable",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${range.startDate} to ${range.endDate}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun BookingDateField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value.ifBlank { "Select a date" },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDatePickerDialog(
    initialDate: String,
    blockedDates: Set<String>,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toUtcEpochMillisOrNull(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = utcTimeMillis.toBackendDateString()
                return !blockedDates.contains(date)
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toBackendDateString()
                        ?.let(onDateSelected)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun List<ListingAvailabilityRangeResponse>.toBlockedDateSet(): Set<String> = buildSet {
    this@toBlockedDateSet.forEach { range ->
        val parsedStart = range.startDate.toUtcEpochMillisOrNull() ?: return@forEach
        val parsedEnd = range.endDate.toUtcEpochMillisOrNull() ?: return@forEach
        if (parsedEnd < parsedStart) return@forEach

        // Walk each inclusive range one UTC day at a time to avoid local-time drift.
        var currentDateMillis = parsedStart
        while (currentDateMillis <= parsedEnd) {
            add(currentDateMillis.toBackendDateString())
            currentDateMillis = currentDateMillis.plusUtcDays(1)
        }
    }
}

private fun hasBlockedDateInRange(
    startDate: String,
    endDate: String,
    blockedDates: Set<String>
): Boolean {
    val start = startDate.toUtcEpochMillisOrNull() ?: return false
    val end = endDate.toUtcEpochMillisOrNull() ?: return false
    if (end < start) return false

    // Any overlap with a blocked day means the booking would conflict with existing availability.
    var currentDateMillis = start
    while (currentDateMillis <= end) {
        if (blockedDates.contains(currentDateMillis.toBackendDateString())) return true
        currentDateMillis = currentDateMillis.plusUtcDays(1)
    }
    return false
}

private fun Long.toBackendDateString(): String = backendDateFormat().format(Date(this))

private fun String.toUtcEpochMillisOrNull(): Long? = try {
    backendDateFormat().parse(this)?.time
} catch (_: ParseException) {
    null
}

private fun Long.plusUtcDays(days: Int): Long {
    val calendar = utcCalendar().apply { timeInMillis = this@plusUtcDays }
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return calendar.timeInMillis
}

private fun backendDateFormat(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }

private fun utcCalendar(): Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
