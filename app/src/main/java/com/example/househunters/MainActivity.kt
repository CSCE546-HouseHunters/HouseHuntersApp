package com.example.househunters

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.screens.CreateListingScreen
import com.example.househunters.ui.screens.Explore
import com.example.househunters.ui.screens.ListingRoute
import com.example.househunters.ui.screens.LoginScreen
import com.example.househunters.ui.screens.SavedScreen
import com.example.househunters.ui.screens.SignupScreen
import com.example.househunters.ui.screens.WelcomeScreen
import com.example.househunters.ui.theme.HouseHuntersTheme
import com.example.househunters.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HouseHuntersTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HouseHuntersApp()
                }
            }
        }
    }
}

@Composable
private fun HouseHuntersApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { HouseHuntersRepository() }
    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModel.factory(context.applicationContext as Application)
    )

    val sessionState = appViewModel.sessionState
    val listingsState = appViewModel.listingsState
    val authState = appViewModel.authState
    val filters = appViewModel.filters

    LaunchedEffect(sessionState.appReady, sessionState.token) {
        if (!sessionState.appReady) return@LaunchedEffect

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val targetRoute = if (sessionState.token.isNullOrBlank()) {
            Screen.Welcome
        } else {
            Screen.Explore
        }

        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(Screen.Welcome) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun navigate(route: String) {
        if (route == Screen.Saved) {
            appViewModel.refreshMyStuff()
        }
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = route == Screen.Explore || route == Screen.Saved || route == Screen.CreateListing
            if (route == Screen.Explore || route == Screen.Saved || route == Screen.CreateListing) {
                popUpTo(Screen.Explore) {
                    saveState = true
                }
            }
        }
    }

    if (!sessionState.appReady) {
        Surface(modifier = Modifier.fillMaxSize()) {}
        return
    }

    NavHost(navController = navController, startDestination = Screen.Welcome) {
        composable(Screen.Welcome) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login) },
                onSignupClick = { navController.navigate(Screen.Signup) }
            )
        }
        composable(Screen.Login) {
            LoginScreen(
                isLoading = authState.loginLoading,
                errorMessage = authState.loginError,
                onLoginClick = { email, password ->
                    appViewModel.login(email, password) {
                        navigate(Screen.Explore)
                    }
                },
                onGotoSignupClick = { navController.navigate(Screen.Signup) }
            )
        }
        composable(Screen.Signup) {
            SignupScreen(
                isLoading = authState.signupLoading,
                errorMessage = authState.signupError,
                onCreateAccountClick = { firstName, lastName, email, phone, password ->
                    appViewModel.register(firstName, lastName, email, phone, password) {
                        navigate(Screen.Explore)
                    }
                },
                onGotoLoginClick = { navController.navigate(Screen.Login) }
            )
        }
        composable(Screen.Explore) {
            Explore(
                listings = appViewModel.visibleListings,
                favoriteIds = listingsState.favoriteIds,
                filters = filters,
                isLoading = listingsState.isLoading,
                errorMessage = listingsState.errorMessage,
                currentUserName = sessionState.user?.firstName,
                onSearchQueryChange = appViewModel::updateSearchQuery,
                onCityChange = appViewModel::updateCity,
                onStateChange = appViewModel::updateState,
                onTypeChange = appViewModel::updateType,
                onMinPriceChange = appViewModel::updateMinPrice,
                onMaxPriceChange = appViewModel::updateMaxPrice,
                onRentalLengthChange = appViewModel::updateRentalLength,
                onApplyFilters = appViewModel::refreshListings,
                onClearFilters = appViewModel::clearFilters,
                onRetry = appViewModel::refreshListings,
                onToggleFavorite = appViewModel::toggleFavorite,
                onOpenListing = { listingId -> navController.navigate(Screen.listing(listingId)) },
                onNavigate = ::navigate,
                onLogout = {
                    appViewModel.logout {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Screen.Saved) {
            SavedScreen(
                listings = appViewModel.savedListings,
                myListings = appViewModel.myListings,
                bookings = appViewModel.myStuffState.bookings,
                favoriteIds = listingsState.favoriteIds,
                isLoading = listingsState.isLoading || appViewModel.myStuffState.isLoading,
                errorMessage = appViewModel.myStuffState.errorMessage ?: listingsState.errorMessage,
                currentUserName = sessionState.user?.firstName,
                isLoggedIn = !sessionState.token.isNullOrBlank(),
                onRetry = {
                    appViewModel.refreshListings()
                    appViewModel.refreshMyStuff()
                },
                onToggleFavorite = appViewModel::toggleFavorite,
                onOpenListing = { listingId -> navController.navigate(Screen.listing(listingId)) },
                onNavigate = ::navigate,
                onLogout = {
                    appViewModel.logout {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Screen.CreateListing) {
            CreateListingScreen(
                repository = repository,
                token = sessionState.token,
                onListingCreated = { createdListing ->
                    appViewModel.refreshListings()
                    navController.navigate(Screen.listing(createdListing.listingId)) {
                        popUpTo(Screen.CreateListing) { inclusive = true }
                    }
                },
                onNavigate = ::navigate,
                onLogout = {
                    appViewModel.logout {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(
            route = Screen.ListingRoute,
            arguments = listOf(navArgument(Screen.ListingIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getInt(Screen.ListingIdArg) ?: return@composable
            ListingRoute(
                listingId = listingId,
                repository = repository,
                token = sessionState.token,
                currentUserId = sessionState.user?.userId,
                favoriteIds = listingsState.favoriteIds,
                onBackClick = { navController.popBackStack() },
                onToggleFavorite = appViewModel::toggleFavorite,
                onListingDeleted = {
                    appViewModel.refreshListings()
                    navController.navigate(Screen.Explore) {
                        popUpTo(Screen.Explore) { inclusive = true }
                    }
                },
                onNavigate = ::navigate,
                onLogout = {
                    appViewModel.logout {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
