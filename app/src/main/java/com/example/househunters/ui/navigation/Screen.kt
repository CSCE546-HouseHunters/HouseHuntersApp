package com.example.househunters.ui.navigation

object Screen {
    const val Welcome = "welcome"
    const val Login = "login"
    const val Signup = "signup"
    const val Explore = "explore"
    const val Saved = "saved"
    const val CreateListing = "create-listing"
    const val Profile = "profile"
    const val Listing = "listing"
    const val ListingIdArg = "listingId"
    const val ListingRoute = "$Listing/{$ListingIdArg}"

    fun listing(listingId: Int): String = "$Listing/$listingId"
}
