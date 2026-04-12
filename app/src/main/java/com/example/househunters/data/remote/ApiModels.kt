package com.example.househunters.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserProfileResponse
)

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UpdateUserProfileRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)

@Serializable
data class ListingImageResponse(
    val listingImageId: Int = 0,
    val imageUrl: String,
    val sortOrder: Int
)

@Serializable
data class ListingImageRequest(
    val imageUrl: String,
    val sortOrder: Int
)

@Serializable
data class ListingSummaryResponse(
    val listingId: Int,
    val userId: Int,
    val address: String,
    val city: String,
    val state: String,
    val zip: String,
    val durationMinDays: Int,
    val durationMaxDays: Int,
    val pricePerDay: Double,
    val type: String,
    val description: String,
    val images: List<ListingImageResponse> = emptyList()
)

@Serializable
data class ListingDetailResponse(
    val listingId: Int,
    val userId: Int,
    val ownerName: String,
    val address: String,
    val city: String,
    val state: String,
    val zip: String,
    val durationMinDays: Int,
    val durationMaxDays: Int,
    val pricePerDay: Double,
    val type: String,
    val description: String,
    val images: List<ListingImageResponse> = emptyList()
)

@Serializable
data class UpsertListingRequest(
    val address: String,
    val city: String,
    val state: String,
    val zip: String,
    val durationMinDays: Int,
    val durationMaxDays: Int,
    val pricePerDay: Double,
    val type: String,
    val description: String,
    val images: List<ListingImageRequest> = emptyList()
)

@Serializable
data class BookingRequest(
    val startDate: String,
    val endDate: String
)

@Serializable
data class BookingStatusUpdateRequest(
    val status: String
)

@Serializable
data class BookingResponse(
    val bookingId: Int,
    val hostUserId: Int,
    val bookerUserId: Int,
    val listingId: Int,
    val startDate: String,
    val endDate: String,
    val status: String
)

@Serializable
data class ListingAvailabilityRangeResponse(
    val startDate: String,
    val endDate: String
)

@Serializable
data class FavoriteResponse(
    val favoriteId: Int,
    val listingId: Int,
    val createdAtUtc: String,
    val listing: ListingSummaryResponse
)
