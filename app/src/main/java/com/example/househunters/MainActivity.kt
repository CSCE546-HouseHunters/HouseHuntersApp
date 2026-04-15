package com.example.househunters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.data.SessionStorage
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.data.remote.UserProfileResponse
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.screens.CreateListingScreen
import com.example.househunters.ui.screens.Explore
import com.example.househunters.ui.screens.ListingRoute
import com.example.househunters.ui.screens.LoginScreen
import com.example.househunters.ui.screens.SavedScreen
import com.example.househunters.ui.screens.SignupScreen
import com.example.househunters.ui.screens.WelcomeScreen
import com.example.househunters.ui.theme.HouseHuntersTheme
import kotlinx.coroutines.launch

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

private data class SessionState(
    val token: String? = null,
    val user: UserProfileResponse? = null
)

@Composable
private fun HouseHuntersApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val repository = remember { HouseHuntersRepository() }
    val sessionStorage = remember(context) { SessionStorage(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var session by remember { mutableStateOf(SessionState()) }
    var listings by remember { mutableStateOf<List<ListingSummaryResponse>>(emptyList()) }
    var favoriteIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var listingsLoading by remember { mutableStateOf(true) }
    var listingsError by remember { mutableStateOf<String?>(null) }
    var loginLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var signupLoading by remember { mutableStateOf(false) }
    var signupError by remember { mutableStateOf<String?>(null) }
    var appReady by remember { mutableStateOf(false) }
    var pendingTopLevelRoute by remember { mutableStateOf<String?>(null) }

    fun refreshListings() {
        scope.launch {
            listingsLoading = true
            listingsError = null
            runCatching {
                repository.getListings()
            }.onSuccess {
                listings = it
            }.onFailure {
                listingsError = it.message ?: "Unable to load listings."
            }
            listingsLoading = false
        }
    }

    fun refreshFavorites(token: String?) {
        if (token.isNullOrBlank()) {
            favoriteIds = emptySet()
            return
        }
        scope.launch {
            runCatching {
                repository.getFavorites(token)
            }.onSuccess {
                favoriteIds = it.map { favorite -> favorite.listingId }.toSet()
            }
        }
    }

    fun completeAuth(token: String, user: UserProfileResponse) {
        session = SessionState(token = token, user = user)
        sessionStorage.writeToken(token)
        refreshFavorites(token)
        refreshListings()
        navController.navigate(Screen.Explore) {
            popUpTo(Screen.Welcome) { inclusive = true }
        }
    }

    fun logout() {
        session = SessionState()
        favoriteIds = emptySet()
        sessionStorage.clear()
        navController.navigate(Screen.Welcome) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun toggleFavorite(listingId: Int) {
        val token = session.token
        if (token.isNullOrBlank()) {
            listingsError = "Log in to save listings."
            return
        }
        scope.launch {
            val isFavorite = favoriteIds.contains(listingId)
            val previous = favoriteIds
            favoriteIds = if (isFavorite) previous - listingId else previous + listingId
            runCatching {
                if (isFavorite) {
                    repository.removeFavorite(listingId, token)
                } else {
                    repository.addFavorite(listingId, token)
                }
            }.onFailure {
                favoriteIds = previous
                listingsError = it.message ?: "Unable to update favorite."
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshListings()
        val savedToken = sessionStorage.readToken()
        if (!savedToken.isNullOrBlank()) {
            runCatching {
                repository.getCurrentUser(savedToken)
            }.onSuccess { user ->
                session = SessionState(token = savedToken, user = user)
                refreshFavorites(savedToken)
                pendingTopLevelRoute = Screen.Explore
            }.onFailure {
                sessionStorage.clear()
            }
        }
        appReady = true
    }

    LaunchedEffect(appReady, pendingTopLevelRoute) {
        val route = pendingTopLevelRoute ?: return@LaunchedEffect
        if (!appReady) return@LaunchedEffect

        navController.navigate(route) {
            popUpTo(Screen.Welcome) { inclusive = true }
        }
        pendingTopLevelRoute = null
    }

    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Screen.Explore) {
                saveState = true
            }
        }
    }

    if (!appReady) {
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
                isLoading = loginLoading,
                errorMessage = loginError,
                onLoginClick = { email, password ->
                    scope.launch {
                        loginLoading = true
                        loginError = null
                        runCatching {
                            repository.login(email, password)
                        }.onSuccess { auth ->
                            completeAuth(auth.token, auth.user)
                        }.onFailure {
                            loginError = it.message ?: "Login failed."
                        }
                        loginLoading = false
                    }
                },
                onGotoSignupClick = { navController.navigate(Screen.Signup) }
            )
        }
        composable(Screen.Signup) {
            SignupScreen(
                isLoading = signupLoading,
                errorMessage = signupError,
                onCreateAccountClick = { firstName, lastName, email, phone, password ->
                    scope.launch {
                        signupLoading = true
                        signupError = null
                        runCatching {
                            repository.register(firstName, lastName, email, phone, password)
                        }.onSuccess { auth ->
                            completeAuth(auth.token, auth.user)
                        }.onFailure {
                            signupError = it.message ?: "Signup failed."
                        }
                        signupLoading = false
                    }
                },
                onGotoLoginClick = { navController.navigate(Screen.Login) }
            )
        }
        composable(Screen.Explore) {
            Explore(
                listings = listings,
                favoriteIds = favoriteIds,
                isLoading = listingsLoading,
                errorMessage = listingsError,
                currentUserName = session.user?.firstName,
                onRetry = { refreshListings() },
                onToggleFavorite = { listingId -> toggleFavorite(listingId) },
                onOpenListing = { listingId -> navController.navigate(Screen.listing(listingId)) },
                onNavigate = { route -> navigateToTopLevel(route) },
                onLogout = { logout() }
            )
        }
        composable(Screen.Saved) {
            SavedScreen(
                listings = listings,
                favoriteIds = favoriteIds,
                isLoading = listingsLoading,
                errorMessage = listingsError,
                currentUserName = session.user?.firstName,
                isLoggedIn = !session.token.isNullOrBlank(),
                onRetry = { refreshListings() },
                onToggleFavorite = { listingId -> toggleFavorite(listingId) },
                onOpenListing = { listingId -> navController.navigate(Screen.listing(listingId)) },
                onNavigate = { route -> navigateToTopLevel(route) },
                onLogout = { logout() }
            )
        }
        composable(Screen.CreateListing) {
            CreateListingScreen(
                repository = repository,
                token = session.token,
                scope = scope,
                onListingCreated = { createdListing ->
                    refreshListings()
                    navController.navigate(Screen.listing(createdListing.listingId)) {
                        popUpTo(Screen.CreateListing) { inclusive = true }
                    }
                },
                onNavigate = { route -> navigateToTopLevel(route) },
                onLogout = { logout() }
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
                token = session.token,
                currentUserId = session.user?.userId,
                favoriteIds = favoriteIds,
                onBackClick = { navController.popBackStack() },
                onToggleFavorite = { id -> toggleFavorite(id) },
                onListingDeleted = {
                    refreshListings()
                    navController.navigate(Screen.Explore) {
                        popUpTo(Screen.Explore) { inclusive = true }
                    }
                },
                onNavigate = { route ->
                    if (
                        route == Screen.Explore ||
                        route == Screen.Saved ||
                        route == Screen.CreateListing
                    ) {
                        navigateToTopLevel(route)
                    }
                },
                onLogout = { logout() }
            )
        }
    }
}
