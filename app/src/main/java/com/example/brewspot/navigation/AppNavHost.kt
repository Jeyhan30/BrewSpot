package com.example.brewspot.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.brewspot.repository.AuthRepository
import com.example.brewspot.view.home.HomeScreen
import com.example.brewspot.view.login.LoginScreen
import com.example.brewspot.view.login.LoginViewModel
import com.example.brewspot.view.login.LoginViewModelFactory
import com.example.brewspot.view.register.RegisterScreen
import com.example.brewspot.view.reservatio.TableLayoutScreen
import com.example.brewspot.view.reservation.TableViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val repository = AuthRepository(auth, firestore)
    val loginViewModelFactory = LoginViewModelFactory(repository)
    val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)

    // Tambahkan ViewModel untuk Table
    val tableViewModel: TableViewModel = viewModel()

    NavHost(navController = navController, startDestination = "reservation") {
        composable("register") {
            RegisterScreen(navController)
        }
        composable("login") {
            LoginScreen(viewModel = loginViewModel, navController = navController)
        }
        composable("reservation") {
            TableLayoutScreen(viewModel = tableViewModel, navController = navController)
        }
        composable("welcome/{username}", arguments = listOf(navArgument("username") {
            type = NavType.StringType
        })) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            WelcomeScreen(username = username ?: "User")
        }
    }
}

@Composable
fun WelcomeScreen(username: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Welcome, $username!", style = MaterialTheme.typography.headlineMedium)
    }
}
