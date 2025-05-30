package com.example.brewspot.view.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Import for viewModel()
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R
import com.example.brewspot.view.home.BottomNavigationBar // Assuming BottomNavigationBar is in home package

// Custom shape for the top brown background, similar to HomeScreen but adapted for Profile
class ProfileTopShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(Path().apply {
        val width = size.width
        val height = size.height

        moveTo(0f, 0f) // Top-left
        lineTo(width, 0f) // Top-right
        lineTo(width, height * 0.7f) // Curve starts slightly higher on the right
        quadraticBezierTo(
            width / 2, height, // Control point at the bottom center, defining the curve
            0f, height * 0.7f // Curve ends slightly higher on the left
        )
        close()
    })
}

// Re-using the decodeBase64ToBitmap function if it's not globally accessible
fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
    if (base64Str.isNullOrEmpty()) {
        return null
    }
    // Hapus awalan "data:image/jpeg;base64," jika ada
    val cleanBase64Str = if (base64Str.startsWith("data:image", ignoreCase = true)) {
        base64Str.substringAfter(",")
    } else {
        base64Str
    }
    return try {
        val decodedBytes = Base64.decode(cleanBase64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel() // Inject ViewModel
) {
    LaunchedEffect(Unit) { // 'Unit' berarti efek ini hanya berjalan sekali saat composable pertama kali masuk komposisi
        // Ini memastikan data di-refresh setiap kali layar profil ditampilkan
        // (asumsi ViewModel mungkin sudah ada dari sesi sebelumnya yang belum dihancurkan)
        profileViewModel.refreshUserProfile()
    }
    val brownColor = Color(0xFF5D4037) // Dark brown color
    val backgroundColor = Color(0xFFF0F0F0) // Light gray background
    val lightGrayBackground = Color(0xFFE0E0E0) // Light gray for the "Masuk" button background
    val darkGrayText = Color(0xFF424242) // Dark gray for text
    val currentUser by profileViewModel.currentUser.collectAsState() // Observe user data
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            // Top Section - Brown Background with User Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // Adjusted height for the profile header
                    .clip(ProfileTopShape())
                    .background(brownColor),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {

                    Text(
                        text = currentUser?.username ?: "Guest", // Display username or "Guest" if null
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.email ?: "guest@example.com", // Display email or default
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                    // Spacer di sini untuk memberi jarak antara teks dan gambar
                    Spacer(modifier = Modifier.height(12.dp)) // Jarak antara teks dan gambar profil
                    val imageModel: Any? = remember(currentUser?.image) {
                        val imageString = currentUser?.image
                        if (imageString.isNullOrEmpty()) {
                            null
                        } else if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                            imageString // Ini adalah URL
                        } else if (imageString.startsWith("data:image/")) {
                            // Ini mungkin Base64, coba dekode
                            decodeBase64ToBitmap(imageString)
                        } else {
                            // Format tidak dikenal, atau mungkin hanya Base64 tanpa awalan data:image
                            decodeBase64ToBitmap(imageString) // Coba dekode sebagai Base64 murni
                        }
                    }

                    val painter = rememberAsyncImagePainter(
                        model = imageModel, // <-- Model sekarang bisa String URL atau Bitmap
                        placeholder = painterResource(id = R.drawable.user), // Placeholder default
                        error = painterResource(id = R.drawable.user) // Gambar error default
                    )
                    Image(
                        painter = painter,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.height(20.dp)) // Spacer di bagian bawah, agar gambar tidak terlalu dekat ke kurva
                }
            }
            // Options Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp) // Move options up to overlap with the curve
                    .padding(horizontal = 24.dp)
            ) {
                ProfileOptionCard(
                    icon = Icons.Default.Person,
                    text = "Ubah Profil",
                    onClick = {
                        navController.navigate("editprofil")
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileOptionCard(
                    icon = Icons.Default.Lock,
                    text = "Ganti Kata Sandi",
                    onClick = { navController.navigate("editsandi" )
                              }
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileOptionCard(
                    icon = painterResource(id = R.drawable.question), // Using QuestionMark as a placeholder for help icon
                    text = "Bantuan dan Dukungan",
                    onClick = { navController.navigate("help_support") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileOptionCard(
                    icon = Icons.Default.Info,
                    text = "Tentang Aplikasi",
                    onClick = { /* TODO: Navigate to About App */ }
                )
                Spacer(modifier = Modifier.height(24.dp)) // Space before logout

                ProfileOptionCard(
                    icon = painterResource(id = R.drawable.logout), // Assuming you have a logout icon in your drawables
                    text = "Logout",
                    onClick = { showLogoutDialog = true } // Show dialog on click
                )

            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false }, // Dismiss when clicking outside or pressing back
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileViewModel.logout() // Call logout function from ViewModel
                        navController.navigate("login") { // Navigate back to login screen after logout
                            popUpTo("login") { // Clear back stack up to login
                                inclusive = true
                            }
                        }
                        showLogoutDialog = false // Dismiss dialog
                    }
                ) {
                    Text("Logout", color = brownColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false } // Dismiss dialog
                ) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White, // Background of the dialog
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray
        )
    }
}


@Composable
fun ProfileOptionCard(
    icon: Any, // Can accept ImageVector or Painter
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor = if (isPressed) Color(0xFF5D4037) else Color.White // Dark brown when pressed, white otherwise
    val textColor = if (isPressed) Color.White else Color.Black // White text when pressed, black otherwise

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Fixed height for option cards
            .clickable(
                interactionSource = interactionSource, // Pass the interactionSource
                indication = null, // Disable ripple effect for custom press effect
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor), // Apply dynamic color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon is ImageVector) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = textColor, // Apply dynamic color
                        modifier = Modifier.size(24.dp)
                    )
                } else if (icon is androidx.compose.ui.graphics.painter.Painter) {
                    Icon(
                        painter = icon,
                        contentDescription = text,
                        tint = textColor, // Apply dynamic color
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    color = textColor, // Apply dynamic color
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next",
                tint = textColor, // Apply dynamic color
                modifier = Modifier.size(24.dp)
            )
        }
    }
}