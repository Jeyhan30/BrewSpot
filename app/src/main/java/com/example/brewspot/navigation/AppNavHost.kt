package com.example.brewspot.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.compose.navigation
import com.example.brewspot.repository.AuthRepository
import com.example.brewspot.view.home.HomeScreen
import com.example.brewspot.view.home.HomeViewModel
import com.example.brewspot.view.login.LoginScreen
import com.example.brewspot.view.login.LoginViewModel
import com.example.brewspot.view.login.LoginViewModelFactory
import com.example.brewspot.view.menu.CartScreen
import com.example.brewspot.view.menu.MenuScreen
import com.example.brewspot.view.menu.MenuViewModel
import com.example.brewspot.view.profile.ChangePasswordScreen
import com.example.brewspot.view.profile.EditProfileScreen
import com.example.brewspot.view.profile.HelpSupportScreen
import com.example.brewspot.view.profile.ProfileScreen
import com.example.brewspot.view.profile.ProfileViewModel
import com.example.brewspot.view.register.RegisterScreen
import com.example.brewspot.view.reservationTest.TableLayoutScreen
import com.example.brewspot.view.reservationTest.TableViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.brewspot.view.cafe_detail.CafeDetailScreen
import com.example.brewspot.view.cafe_detail.CafeDetailViewModel
import java.net.URLDecoder // Import untuk URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Inisialisasi dependensi dan ViewModel
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val repository = AuthRepository(auth, firestore)
    val loginViewModelFactory = LoginViewModelFactory(repository)

    // Inisialisasi ViewModel menggunakan factory atau default
    val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)
    val homeViewModel: HomeViewModel = viewModel()
    val cafeDetailViewModel: CafeDetailViewModel = viewModel()
    val tableViewModel: TableViewModel = viewModel() // ViewModel untuk TableLayoutScreen
    val profileViewModel: ProfileViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel() // <-- Correctly initialize HomeViewModel here
    val menuViewModel: MenuViewModel = viewModel() // Initialize MenuViewModel here


    NavHost(navController = navController, startDestination = "login") {
        composable("register") {
            RegisterScreen(navController)
        }
        composable("login") {
            LoginScreen(viewModel = loginViewModel, navController = navController)
        }
        composable("reservation") {
            TableLayoutScreen(viewModel = tableViewModel, navController = navController)
        }
        composable("profile") {
            // Pass the profileViewModel here
            ProfileScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable("editprofil") {
            // Pass the profileViewModel here
            EditProfileScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable("editsandi") {
            // Pass the profileViewModel here
            ChangePasswordScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable("help_support") { // <--- ADD THIS NEW ROUTE
            HelpSupportScreen(navController = navController)
        }
        composable("welcome/{username}", arguments = listOf(navArgument("username") {
            type = NavType.StringType
        })) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
        // Rute untuk HomeScreen dengan parameter username (jika ada)
        composable(
            "welcome/{username}",
            arguments = listOf(navArgument("username") {
                type = NavType.StringType
                defaultValue = "User" // Default value jika tidak ada username
            })
        ) { backStackEntry ->
            // Anda bisa menggunakan username di HomeScreen jika diperlukan
            // val username = backStackEntry.arguments?.getString("username")
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }
        // Rute alternatif untuk HomeScreen tanpa parameter username
        composable("welcome") {
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }

        // Rute untuk CafeDetailScreen dengan parameter cafeId
        composable(
            "cafeDetail/{cafeId}",
            arguments = listOf(navArgument("cafeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId")
            CafeDetailScreen(navController = navController, cafeId = cafeId, viewModel = cafeDetailViewModel)
        }
        composable("menu/{cafeId}") { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: "default"
            MenuScreen(navController = navController, cafeId = cafeId, menuViewModel = menuViewModel)
        }
        // MODIFIED: cart_screen now accepts a nullable cafeId argument
        composable(
            route = "cart_screen?cafeId={cafeId}",
            arguments = listOf(navArgument("cafeId") {
                type = NavType.StringType
                nullable = true // Allow cafeId to be null if navigating to cart from other places
            })
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId")
            CartScreen(navController = navController, menuViewModel = menuViewModel, cafeId = cafeId)
        }


}

        // Rute untuk TableLayoutScreen dengan cafeId sebagai path parameter
        // dan parameter reservasi lainnya sebagai query parameters
        composable(
            "tableLayout/{cafeId}?" + // cafeId sebagai path parameter
                    "userName={userName}&" +
                    "date={date}&" +
                    "time={time}&" +
                    "totalGuests={totalGuests}",
            arguments = listOf(
                navArgument("cafeId") { type = NavType.StringType },
                // Query parameters bisa diset sebagai nullable (true) atau dengan defaultValue
                navArgument("userName") { type = NavType.StringType; nullable = true },
                navArgument("date") { type = NavType.StringType; nullable = true },
                navArgument("time") { type = NavType.StringType; nullable = true },
                navArgument("totalGuests") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId")
            // Parameter kueri secara otomatis didekode oleh NavController.
            // Anda tidak perlu URLDecoder.decode() lagi di sini.
            val userName = backStackEntry.arguments?.getString("userName")
            val date = backStackEntry.arguments?.getString("date")
            val time = backStackEntry.arguments?.getString("time")
            val totalGuests = backStackEntry.arguments?.getInt("totalGuests") ?: 0

            TableLayoutScreen(
                navController = navController,
                cafeId = cafeId,
                userName = userName,
                date = date,
                time = time,
                totalGuests = totalGuests,
                viewModel = tableViewModel // Menggunakan TableViewModel yang sudah diinisialisasi
            )
        }

        composable("profile") {
            ProfileScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable("editprofil") {
            EditProfileScreen(navController = navController, profileViewModel = profileViewModel)
        }
        // Rute untuk layar riwayat (jika ada)
        composable("history") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Riwayat Anda akan muncul di sini.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        // Rute untuk layar konfirmasi reservasi (setelah booking)
        composable("confirmation_screen") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Reservasi Anda berhasil dikonfirmasi!", style = MaterialTheme.typography.headlineMedium)
            }
        }
        // Anda bisa menambahkan rute lain di sini
    }
}