package com.example.househunters.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.data.remote.BookingResponse
import com.example.househunters.data.remote.ListingAvailabilityRangeResponse
import com.example.househunters.data.remote.ListingDetailResponse
import kotlinx.coroutines.launch

data class ListingUiState(
    val listing: ListingDetailResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val availabilityRanges: List<ListingAvailabilityRangeResponse> = emptyList(),
    val availabilityLoading: Boolean = false,
    val availabilityError: String? = null,
    val bookingMessage: String? = null,
    val isBooking: Boolean = false,
    val isDeleting: Boolean = false,
    val ownerBookings: List<BookingResponse> = emptyList(),
    val ownerBookingsLoading: Boolean = false,
    val ownerBookingsError: String? = null,
    val activeBookingStatusId: Int? = null
)

class ListingViewModel(
    private val listingId: Int,
    private val repository: HouseHuntersRepository,
    private val token: String?,
    private val currentUserId: Int?
) : ViewModel() {
    var uiState by mutableStateOf(ListingUiState())
        private set

    init {
        loadListing()
        loadAvailability()
    }

    fun createBooking(startDate: String, endDate: String) {
        val authToken = token
        if (authToken.isNullOrBlank()) {
            uiState = uiState.copy(bookingMessage = "Log in before creating a booking.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isBooking = true, bookingMessage = null)
            runCatching {
                repository.createBooking(
                    listingId = listingId,
                    startDate = startDate,
                    endDate = endDate,
                    token = authToken
                )
            }.onSuccess { booking ->
                uiState = uiState.copy(
                    isBooking = false,
                    bookingMessage = "Booking created: ${booking.status}"
                )
                loadAvailability()
            }.onFailure { error ->
                uiState = uiState.copy(
                    isBooking = false,
                    bookingMessage = error.message ?: "Unable to create booking."
                )
            }
        }
    }

    fun deleteListing(onDeleted: () -> Unit) {
        val authToken = token
        if (authToken.isNullOrBlank()) {
            uiState = uiState.copy(bookingMessage = "Log in again before deleting this listing.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isDeleting = true, bookingMessage = null)
            runCatching {
                repository.deleteListing(listingId, authToken)
            }.onSuccess {
                uiState = uiState.copy(isDeleting = false)
                onDeleted()
            }.onFailure { error ->
                uiState = uiState.copy(
                    isDeleting = false,
                    bookingMessage = error.message ?: "Unable to delete listing."
                )
            }
        }
    }

    fun updateOwnerBookingStatus(bookingId: Int, status: String) {
        val authToken = token
        if (authToken.isNullOrBlank()) {
            uiState = uiState.copy(ownerBookingsError = "Log in again before managing bookings.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                activeBookingStatusId = bookingId,
                ownerBookingsError = null
            )
            runCatching {
                repository.updateBookingStatus(bookingId, status, authToken)
            }.onSuccess { updatedBooking ->
                uiState = uiState.copy(
                    ownerBookings = uiState.ownerBookings.map { booking ->
                        if (booking.bookingId == bookingId) updatedBooking else booking
                    },
                    activeBookingStatusId = null,
                    bookingMessage = "Booking ${updatedBooking.bookingId} updated to ${updatedBooking.status}."
                )
                loadAvailability()
            }.onFailure { error ->
                uiState = uiState.copy(
                    activeBookingStatusId = null,
                    ownerBookingsError = error.message ?: "Unable to update booking status."
                )
            }
        }
    }

    private fun loadListing() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching {
                repository.getListingDetails(listingId)
            }.onSuccess { listing ->
                uiState = uiState.copy(
                    listing = listing,
                    isLoading = false,
                    errorMessage = null
                )
                if (currentUserId == listing.userId && !token.isNullOrBlank()) {
                    loadOwnerBookings()
                }
            }.onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load listing."
                )
            }
        }
    }

    private fun loadAvailability() {
        viewModelScope.launch {
            uiState = uiState.copy(availabilityLoading = true, availabilityError = null)
            runCatching {
                repository.getListingAvailability(listingId)
            }.onSuccess { availability ->
                uiState = uiState.copy(
                    availabilityRanges = availability.sortedBy(ListingAvailabilityRangeResponse::startDate),
                    availabilityLoading = false,
                    availabilityError = null
                )
            }.onFailure { error ->
                uiState = uiState.copy(
                    availabilityRanges = emptyList(),
                    availabilityLoading = false,
                    availabilityError = error.message ?: "Unable to load unavailable dates."
                )
            }
        }
    }

    private fun loadOwnerBookings() {
        val authToken = token ?: return
        viewModelScope.launch {
            uiState = uiState.copy(ownerBookingsLoading = true, ownerBookingsError = null)
            runCatching {
                repository.getListingBookings(listingId, authToken)
            }.onSuccess { bookings ->
                uiState = uiState.copy(
                    ownerBookings = bookings.sortedBy(BookingResponse::startDate),
                    ownerBookingsLoading = false,
                    ownerBookingsError = null
                )
            }.onFailure { error ->
                uiState = uiState.copy(
                    ownerBookingsLoading = false,
                    ownerBookingsError = error.message ?: "Unable to load booking requests."
                )
            }
        }
    }

    companion object {
        fun factory(
            listingId: Int,
            repository: HouseHuntersRepository,
            token: String?,
            currentUserId: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ListingViewModel(
                    listingId = listingId,
                    repository = repository,
                    token = token,
                    currentUserId = currentUserId
                ) as T
            }
        }
    }
}
