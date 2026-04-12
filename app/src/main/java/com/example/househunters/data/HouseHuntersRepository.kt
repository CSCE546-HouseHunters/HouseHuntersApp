package com.example.househunters.data

import com.example.househunters.data.remote.AuthResponse
import com.example.househunters.data.remote.BookingRequest
import com.example.househunters.data.remote.BookingResponse
import com.example.househunters.data.remote.BookingStatusUpdateRequest
import com.example.househunters.data.remote.FavoriteResponse
import com.example.househunters.data.remote.HouseHuntersApi
import com.example.househunters.data.remote.ListingDetailResponse
import com.example.househunters.data.remote.ListingAvailabilityRangeResponse
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.data.remote.LoginRequest
import com.example.househunters.data.remote.RegisterRequest
import com.example.househunters.data.remote.UpdateUserProfileRequest
import com.example.househunters.data.remote.UpsertListingRequest
import com.example.househunters.data.remote.UserProfileResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class HouseHuntersRepository(
    private val api: HouseHuntersApi = HouseHuntersApi()
) {
    suspend fun login(email: String, password: String): AuthResponse =
        api.post("/api/auth/login", LoginRequest(email = email, password = password))

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): AuthResponse = api.post(
        "/api/auth/register",
        RegisterRequest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            password = password
        )
    )

    suspend fun getCurrentUser(token: String): UserProfileResponse =
        api.get("/api/users/me", token)

    suspend fun updateCurrentUser(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        token: String
    ): UserProfileResponse = api.put(
        "/api/users/me",
        UpdateUserProfileRequest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone
        ),
        token
    )

    suspend fun getListings(
        city: String? = null,
        state: String? = null,
        zip: String? = null,
        type: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        minDays: Int? = null,
        maxDays: Int? = null
    ): List<ListingSummaryResponse> {
        val urlBuilder = "${HouseHuntersApi.BASE_URL}/api/listings".toHttpUrlOrNull()
            ?.newBuilder()
            ?: error("Invalid listings URL")

        listOf(
            "city" to city,
            "state" to state,
            "zip" to zip,
            "type" to type
        ).forEach { (name, value) ->
            if (!value.isNullOrBlank()) {
                urlBuilder.addQueryParameter(name, value)
            }
        }

        minPrice?.let { urlBuilder.addQueryParameter("minPrice", it.toString()) }
        maxPrice?.let { urlBuilder.addQueryParameter("maxPrice", it.toString()) }
        minDays?.let { urlBuilder.addQueryParameter("minDays", it.toString()) }
        maxDays?.let { urlBuilder.addQueryParameter("maxDays", it.toString()) }

        val path = urlBuilder.build().encodedPath +
            if (urlBuilder.build().encodedQuery.isNullOrBlank()) "" else "?${urlBuilder.build().encodedQuery}"

        return api.get(path)
    }

    suspend fun getListingDetails(listingId: Int): ListingDetailResponse =
        api.get("/api/listings/$listingId")

    suspend fun getListingAvailability(listingId: Int): List<ListingAvailabilityRangeResponse> =
        api.get("/api/listings/$listingId/availability")

    suspend fun createListing(
        request: UpsertListingRequest,
        token: String
    ): ListingDetailResponse = api.post("/api/listings", request, token)

    suspend fun updateListing(
        listingId: Int,
        request: UpsertListingRequest,
        token: String
    ): ListingDetailResponse = api.put("/api/listings/$listingId", request, token)

    suspend fun deleteListing(listingId: Int, token: String) {
        api.delete<Unit>("/api/listings/$listingId", token)
    }

    suspend fun getFavorites(token: String): List<FavoriteResponse> =
        api.get("/api/favorites", token)

    suspend fun addFavorite(listingId: Int, token: String): FavoriteResponse =
        api.post("/api/listings/$listingId/favorite", emptyMap<String, String>(), token)

    suspend fun removeFavorite(listingId: Int, token: String) {
        api.delete<Unit>("/api/listings/$listingId/favorite", token)
    }

    suspend fun createBooking(
        listingId: Int,
        startDate: String,
        endDate: String,
        token: String
    ): BookingResponse = api.post(
        "/api/listings/$listingId/bookings",
        BookingRequest(startDate = startDate, endDate = endDate),
        token
    )

    suspend fun getMyBookings(token: String): List<BookingResponse> =
        api.get("/api/bookings/me", token)

    suspend fun getListingBookings(listingId: Int, token: String): List<BookingResponse> =
        api.get("/api/listings/$listingId/bookings", token)

    suspend fun updateBookingStatus(
        bookingId: Int,
        status: String,
        token: String
    ): BookingResponse = api.patch(
        "/api/bookings/$bookingId/status",
        BookingStatusUpdateRequest(status),
        token
    )
}
