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
import com.example.brewspot.view.confirmation.ConfirmationPaymentScreen
import com.example.brewspot.view.confirmation.ConfirmationViewModel
import com.example.brewspot.view.history.HistoryScreen
import com.example.brewspot.view.history.HistoryViewModel
import com.example.brewspot.view.payment.PaymentMethodScreen
import com.example.brewspot.view.payment.PaymentMethodViewModel
import com.example.brewspot.view.profile.TentangScreen
import com.example.brewspot.view.voucher.VoucherScreen
import java.net.URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val repository = AuthRepository(auth, firestore)
    val loginViewModelFactory = LoginViewModelFactory(repository)
    val historyViewModel: HistoryViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)
    val cafeDetailViewModel: CafeDetailViewModel = viewModel()
    val tableViewModel: TableViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val menuViewModel: MenuViewModel = viewModel()
    val confirmationViewModel: ConfirmationViewModel = viewModel()
    val paymentMethodViewModel: PaymentMethodViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("register") {
            RegisterScreen(navController)
        }
        composable("login") {
            LoginScreen(viewModel = loginViewModel, navController = navController)
        }

        composable("profile") {
            ProfileScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable("editprofil") {
            EditProfileScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable("editsandi") {
            ChangePasswordScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable("help_support") {
            HelpSupportScreen(navController = navController)
        }
        composable("welcome/{username}", arguments = listOf(navArgument("username") {
            type = NavType.StringType
        })) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }
        composable("welcome"){
            HomeScreen(viewModel = homeViewModel, navController = navController)

        }
        composable(
            "cafeDetail/{cafeId}",
            arguments = listOf(navArgument("cafeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId")
            CafeDetailScreen(
                navController = navController,
                cafeId = cafeId,
                viewModel = cafeDetailViewModel
            )
        }
        composable("menu/{cafeId}?reservationId={reservationId}", arguments = listOf(
            navArgument("cafeId") { type = NavType.StringType },
            navArgument("reservationId") { type = NavType.StringType; nullable = true }
        )) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: "default"
            val reservationId = backStackEntry.arguments?.getString("reservationId")
            MenuScreen(
                navController = navController,
                cafeId = cafeId,
                reservationId = reservationId,
                menuViewModel = menuViewModel
            )
        }

        composable(
            route = "cart_screen?cafeId={cafeId}",
            arguments = listOf(navArgument("cafeId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId")
            CartScreen(
                navController = navController,
                menuViewModel = menuViewModel,
                cafeId = cafeId
            )
        }

        composable(
            "confirmation_payment?cafeId={cafeId}&reservationId={reservationId}",
            arguments = listOf(
                navArgument("cafeId") { type = NavType.StringType },
                navArgument("reservationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId")
            val reservationId = backStackEntry.arguments?.getString("reservationId")
            if (cafeId != null && reservationId != null) {
                ConfirmationPaymentScreen(
                    navController = navController,
                    cafeId = cafeId,
                    reservationId = reservationId,
                    viewModel = confirmationViewModel,
                    menuViewModel = menuViewModel,
                    paymentMethodViewModel = paymentMethodViewModel
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: Data konfirmasi tidak lengkap.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        composable("voucherScreen") {
            VoucherScreen(navController = navController, confirmationViewModel = confirmationViewModel)
        }

        composable("paymentMethodScreen") {
            PaymentMethodScreen(
                navController = navController,
                paymentMethodViewModel = paymentMethodViewModel
            )
        }
    composable(
        "tableLayout/{cafeId}?" +
                "userName={userName}&" +
                "date={date}&" +
                "time={time}&" +
                "totalGuests={totalGuests}",
        arguments = listOf(
            navArgument("cafeId") { type = NavType.StringType },
            navArgument("userName") { type = NavType.StringType; nullable = true },
            navArgument("date") { type = NavType.StringType; nullable = true },
            navArgument("time") { type = NavType.StringType; nullable = true },
            navArgument("totalGuests") { type = NavType.IntType; defaultValue = 0 }
        )
    ) { backStackEntry ->
        val cafeId = backStackEntry.arguments?.getString("cafeId")
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
            viewModel = tableViewModel
        )
    }

    composable("profile") {
        ProfileScreen(navController = navController, profileViewModel = profileViewModel)
    }
    composable("editprofil") {
        EditProfileScreen(navController = navController, profileViewModel = profileViewModel)
    }
    composable("history") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Riwayat Anda akan muncul di sini.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
    composable("confirmation_screen") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reservasi Anda berhasil dikonfirmasi!",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
        composable("history") {
            HistoryScreen(navController = navController, viewModel = historyViewModel)
        }

        composable("about_app") {
            TentangScreen(navController = navController)
        }
    }
}