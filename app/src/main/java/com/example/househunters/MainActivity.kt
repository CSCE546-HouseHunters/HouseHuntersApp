package com.example.househunters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.househunters.ui.navigation.Screen
import com.example.househunters.ui.screens.Explore
import com.example.househunters.ui.screens.LoginScreen
import com.example.househunters.ui.screens.SignupScreen
import com.example.househunters.ui.screens.WelcomeScreen
import com.example.househunters.ui.theme.HouseHuntersTheme

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

    NavHost(navController = navController, startDestination = Screen.Welcome) {
        composable(Screen.Welcome) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login) },
                onSignupClick = { navController.navigate(Screen.Signup) }
            )
        }
        composable(Screen.Login) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Explore) {
                        popUpTo(Screen.Welcome) { inclusive = true }
                    }
                },
                onGotoSignupClick = { navController.navigate(Screen.Signup) }
            )
        }
        composable(Screen.Signup) {
            SignupScreen(
                onCreateAccountClick = {
                    navController.navigate(Screen.Explore) {
                        popUpTo(Screen.Welcome) { inclusive = true }
                    }
                },
                onGotoLoginClick = { navController.navigate(Screen.Login) }
            )
        }
        composable(Screen.Explore) {
            Explore()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HouseHuntersPreview() {
    HouseHuntersTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HouseHuntersApp()
        }
    }
}
