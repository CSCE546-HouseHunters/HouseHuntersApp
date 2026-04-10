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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.househunters.data.HouseHuntersRepository
import com.example.househunters.data.remote.ListingSummaryResponse
import com.example.househunters.data.remote.UserProfileResponse
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.screens.Explore
import com.example.househunters.ui.screens.ListingRoute
import com.example.househunters.ui.screens.LoginScreen
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
    val navController = rememberNavController()
    val repository = remember { HouseHuntersRepository() }
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
        refreshFavorites(token)
        refreshListings()
        navController.navigate(Screen.Explore) {
            popUpTo(Screen.Welcome) { inclusive = true }
        }
    }

    fun toggleFavorite(listingId: Int) {
        val token = session.token ?: return
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
                onNavigate = { route ->
                    if (route == Screen.Explore) {
                        navController.navigate(Screen.Explore)
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
                token = session.token,
                favoriteIds = favoriteIds,
                onBackClick = { navController.popBackStack() },
                onToggleFavorite = { id -> toggleFavorite(id) },
                onNavigate = { route ->
                    if (route == Screen.Explore) {
                        navController.navigate(Screen.Explore)
                    }
                }
            )
        }
    }
}
