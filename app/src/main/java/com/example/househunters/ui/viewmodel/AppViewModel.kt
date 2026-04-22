package com.example.househunters.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.data.SessionStorage
import com.example.househunters.data.remote.BookingResponse
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.data.remote.UserProfileResponse
import kotlinx.coroutines.launch

enum class RentalLengthFilter {
    ALL,
    WEEKEND,
    LONG_TERM
}

data class SessionUiState(
    val token: String? = null,
    val user: UserProfileResponse? = null,
    val appReady: Boolean = false
)

data class ListingsFilterState(
    val query: String = "",
    val city: String = "",
    val state: String = "",
    val type: String = "",
    val minPrice: String = "",
    val maxPrice: String = "",
    val rentalLength: RentalLengthFilter = RentalLengthFilter.ALL
)

data class ListingsUiState(
    val listings: List<ListingSummaryResponse> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class AuthUiState(
    val loginLoading: Boolean = false,
    val loginError: String? = null,
    val signupLoading: Boolean = false,
    val signupError: String? = null
)

data class ProfileUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class MyStuffUiState(
    val bookings: List<BookingResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AppViewModel(
    private val repository: HouseHuntersRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {
    var sessionState by mutableStateOf(SessionUiState())
        private set

    var listingsState by mutableStateOf(ListingsUiState(isLoading = true))
        private set

    var authState by mutableStateOf(AuthUiState())
        private set

    var profileState by mutableStateOf(ProfileUiState())
        private set

    var myStuffState by mutableStateOf(MyStuffUiState())
        private set

    var filters by mutableStateOf(ListingsFilterState())
        private set

    init {
        bootstrap()
    }

    val visibleListings: List<ListingSummaryResponse>
        get() = listingsState.listings
            .filterByQuery(filters.query)
            .filterByCity(filters.city)
            .filterByState(filters.state)
            .filterByType(filters.type)
            .filterByMinPrice(filters.minPrice)
            .filterByMaxPrice(filters.maxPrice)
            .filterByRentalLength(filters.rentalLength)

    val savedListings: List<ListingSummaryResponse>
        get() = visibleListings.filter { listing ->
            listingsState.favoriteIds.contains(listing.listingId)
        }

    val myListings: List<ListingSummaryResponse>
        get() {
            val currentUserId = sessionState.user?.userId ?: return emptyList()
            return listingsState.listings.filter { it.userId == currentUserId }
        }

    fun updateSearchQuery(query: String) {
        filters = filters.copy(query = query)
    }

    fun updateCity(city: String) {
        filters = filters.copy(city = city)
    }

    fun updateState(state: String) {
        filters = filters.copy(state = state.take(2).uppercase())
    }

    fun updateType(type: String) {
        filters = filters.copy(type = type)
    }

    fun updateMinPrice(minPrice: String) {
        filters = filters.copy(minPrice = minPrice.filterAllowedDecimal())
    }

    fun updateMaxPrice(maxPrice: String) {
        filters = filters.copy(maxPrice = maxPrice.filterAllowedDecimal())
    }

    fun updateRentalLength(rentalLength: RentalLengthFilter) {
        filters = filters.copy(rentalLength = rentalLength)
    }

    fun clearFilters() {
        filters = ListingsFilterState()
        refreshListings()
    }

    fun refreshListings() {
        viewModelScope.launch {
            listingsState = listingsState.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                repository.getListings()
            }.onSuccess { listings ->
                listingsState = listingsState.copy(
                    listings = listings,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                listingsState = listingsState.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load listings."
                )
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authState = authState.copy(loginLoading = true, loginError = null)
            runCatching {
                repository.login(email, password)
            }.onSuccess { auth ->
                completeAuth(auth.token, auth.user)
                onSuccess()
            }.onFailure { error ->
                authState = authState.copy(
                    loginError = error.message ?: "Login failed."
                )
            }
            authState = authState.copy(loginLoading = false)
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            authState = authState.copy(signupLoading = true, signupError = null)
            runCatching {
                repository.register(firstName, lastName, email, phone, password)
            }.onSuccess { auth ->
                completeAuth(auth.token, auth.user)
                onSuccess()
            }.onFailure { error ->
                authState = authState.copy(
                    signupError = error.message ?: "Signup failed."
                )
            }
            authState = authState.copy(signupLoading = false)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        sessionState = SessionUiState(appReady = true)
        listingsState = ListingsUiState(
            listings = listingsState.listings,
            isLoading = false
        )
        profileState = ProfileUiState()
        myStuffState = MyStuffUiState()
        sessionStorage.clear()
        onLoggedOut()
    }

    fun clearProfileMessages() {
        profileState = profileState.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val token = sessionState.token
        val currentUser = sessionState.user

        val validationError = validateProfileForm(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone
        )
        if (validationError != null) {
            profileState = profileState.copy(
                errorMessage = validationError,
                successMessage = null
            )
            return
        }

        if (token.isNullOrBlank() || currentUser == null) {
            profileState = profileState.copy(
                errorMessage = "Log in again before updating your profile.",
                successMessage = null
            )
            return
        }

        viewModelScope.launch {
            profileState = profileState.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            runCatching {
                repository.updateCurrentUser(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    token = token
                )
            }.onSuccess { updatedUser ->
                sessionState = sessionState.copy(user = updatedUser)
                profileState = profileState.copy(
                    isSaving = false,
                    errorMessage = null,
                    successMessage = "Profile updated."
                )
                onSuccess?.invoke()
            }.onFailure { error ->
                profileState = profileState.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Unable to update your profile.",
                    successMessage = null
                )
            }
        }
    }

    fun refreshMyStuff() {
        val token = sessionState.token
        if (token.isNullOrBlank()) {
            myStuffState = MyStuffUiState()
            listingsState = listingsState.copy(favoriteIds = emptySet())
            return
        }

        refreshFavorites(token)
        refreshBookings(token)
    }

    fun toggleFavorite(listingId: Int) {
        val token = sessionState.token
        if (token.isNullOrBlank()) {
            listingsState = listingsState.copy(errorMessage = "Log in to save listings.")
            return
        }

        viewModelScope.launch {
            val previous = listingsState.favoriteIds
            val isFavorite = previous.contains(listingId)
            listingsState = listingsState.copy(
                favoriteIds = if (isFavorite) previous - listingId else previous + listingId
            )

            runCatching {
                if (isFavorite) {
                    repository.removeFavorite(listingId, token)
                } else {
                    repository.addFavorite(listingId, token)
                }
            }.onFailure { error ->
                listingsState = listingsState.copy(
                    favoriteIds = previous,
                    errorMessage = error.message ?: "Unable to update favorite."
                )
            }
        }
    }

    private fun bootstrap() {
        viewModelScope.launch {
            refreshListings()

            val savedToken = sessionStorage.readToken()
            if (!savedToken.isNullOrBlank()) {
                runCatching {
                    repository.getCurrentUser(savedToken)
                }.onSuccess { user ->
                    sessionState = SessionUiState(
                        token = savedToken,
                        user = user,
                        appReady = true
                    )
                    refreshMyStuff()
                }.onFailure {
                    sessionStorage.clear()
                    sessionState = SessionUiState(appReady = true)
                }
            } else {
                sessionState = SessionUiState(appReady = true)
            }
        }
    }

    private fun completeAuth(token: String, user: UserProfileResponse) {
        sessionState = SessionUiState(token = token, user = user, appReady = true)
        sessionStorage.writeToken(token)
        refreshMyStuff()
        refreshListings()
    }

    private fun refreshFavorites(token: String) {
        viewModelScope.launch {
            runCatching {
                repository.getFavorites(token)
            }.onSuccess { favorites ->
                listingsState = listingsState.copy(
                    favoriteIds = favorites.map { it.listingId }.toSet()
                )
            }
        }
    }

    private fun refreshBookings(token: String) {
        viewModelScope.launch {
            myStuffState = myStuffState.copy(isLoading = true, errorMessage = null)
            runCatching {
                repository.getMyBookings(token)
            }.onSuccess { bookings ->
                myStuffState = myStuffState.copy(
                    bookings = bookings.sortedByDescending(BookingResponse::startDate),
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                myStuffState = myStuffState.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load your bookings."
                )
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(
                        repository = HouseHuntersRepository(),
                        sessionStorage = SessionStorage(application.applicationContext)
                    ) as T
                }
            }
    }
}

private fun List<ListingSummaryResponse>.filterByCity(city: String): List<ListingSummaryResponse> {
    if (city.isBlank()) return this
    val normalizedCity = city.normalizeForSearch()
    return filter { listing ->
        listing.city.normalizeForSearch().contains(normalizedCity)
    }
}

private fun List<ListingSummaryResponse>.filterByState(state: String): List<ListingSummaryResponse> {
    if (state.isBlank()) return this
    val normalizedState = state.normalizeForSearch()
    return filter { listing ->
        listing.state.normalizeForSearch().contains(normalizedState)
    }
}

private fun List<ListingSummaryResponse>.filterByType(type: String): List<ListingSummaryResponse> {
    if (type.isBlank()) return this
    val normalizedType = type.normalizeForSearch()
    return filter { listing ->
        listing.type.normalizeForSearch().contains(normalizedType)
    }
}

private fun List<ListingSummaryResponse>.filterByMinPrice(minPrice: String): List<ListingSummaryResponse> {
    val parsedMinPrice = minPrice.toDoubleOrNull() ?: return this
    return filter { listing -> listing.pricePerDay >= parsedMinPrice }
}

private fun List<ListingSummaryResponse>.filterByMaxPrice(maxPrice: String): List<ListingSummaryResponse> {
    val parsedMaxPrice = maxPrice.toDoubleOrNull() ?: return this
    return filter { listing -> listing.pricePerDay <= parsedMaxPrice }
}

private fun List<ListingSummaryResponse>.filterByQuery(
    query: String
): List<ListingSummaryResponse> {
    if (query.isBlank()) return this

    val normalizedTerms = query.toSearchTerms()

    if (normalizedTerms.isEmpty()) return this

    return filter { listing ->
        val searchableText = buildString {
            append(listing.address)
            append(' ')
            append(listing.city)
            append(' ')
            append(listing.state)
            append(' ')
            append(listing.zip)
            append(' ')
            append(listing.type)
            append(' ')
            append(listing.description)
        }
            .normalizeForSearch()

        normalizedTerms.all { term -> searchableText.contains(term) }
    }
}

private fun String.toSearchTerms(): List<String> =
    normalizeForSearch()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

private fun String.normalizeForSearch(): String =
    lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()

private fun List<ListingSummaryResponse>.filterByRentalLength(
    filter: RentalLengthFilter
): List<ListingSummaryResponse> = when (filter) {
    RentalLengthFilter.ALL -> this

    // Weekend stays should allow short visits without forcing a long minimum.
    RentalLengthFilter.WEEKEND -> filter { listing -> listing.durationMinDays <= 3 }

    // Long-term stays target listings that are designed around month-scale bookings.
    RentalLengthFilter.LONG_TERM -> filter { listing -> listing.durationMaxDays >= 28 }
}

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

private fun validateProfileForm(
    firstName: String,
    lastName: String,
    email: String,
    phone: String
): String? {
    if (firstName.isBlank() || lastName.isBlank()) {
        return "Enter both your first and last name."
    }
    if (email.isBlank()) {
        return "Enter your email address."
    }
    if (!EMAIL_REGEX.matches(email.trim())) {
        return "Enter a valid email address."
    }
    val normalizedPhone = phone.filter(Char::isDigit)
    if (normalizedPhone.length !in 10..15) {
        return "Enter a valid phone number."
    }
    return null
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
