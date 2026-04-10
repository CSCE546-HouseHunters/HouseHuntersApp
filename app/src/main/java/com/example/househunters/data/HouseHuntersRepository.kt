package com.example.househunters.data

import com.example.househunters.data.remote.AuthResponse
import com.example.househunters.data.remote.BookingRequest
import com.example.househunters.data.remote.BookingResponse
import com.example.househunters.data.remote.FavoriteResponse
import com.example.househunters.data.remote.HouseHuntersApi
import com.example.househunters.data.remote.ListingDetailResponse
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.data.remote.LoginRequest
import com.example.househunters.data.remote.RegisterRequest
import com.example.househunters.data.remote.UserProfileResponse

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

    suspend fun getListings(): List<ListingSummaryResponse> =
        api.get("/api/listings")

    suspend fun getListingDetails(listingId: Int): ListingDetailResponse =
        api.get("/api/listings/$listingId")

    suspend fun getFavorites(token: String): List<FavoriteResponse> =
        api.get("/api/favorites", token)

    suspend fun addFavorite(listingId: Int, token: String): FavoriteResponse =
        api.post("/api/listings/$listingId/favorite", emptyMap<String, String>(), token)

    suspend fun removeFavorite(listingId: Int, token: String) {
        api.request<Unit, String>("DELETE", "/api/listings/$listingId/favorite", token = token)
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
}
