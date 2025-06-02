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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R
import com.example.brewspot.view.home.BottomNavigationBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
class ProfileTopShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(Path().apply {
        val width = size.width
        val height = size.height

        moveTo(0f, 0f)
        lineTo(width, 0f)
        lineTo(width, height * 0.7f)
        quadraticBezierTo(
            width / 2, height,
            0f, height * 0.7f
        )
        close()
    })
}

fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
    if (base64Str.isNullOrEmpty()) {
        return null
    }
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
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("46336032851-psnppj5vbnt7oq5ri5c7qn0u7q4knoj9.apps.googleusercontent.com") // Gunakan ID proyek Anda yang sebenarnya
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    LaunchedEffect(Unit) {
        profileViewModel.refreshUserProfile()
    }
    val brownColor = Color(0xFF5D4037)
    val backgroundColor = Color(0xFFF0F0F0)
    val lightGrayBackground = Color(0xFFE0E0E0)
    val darkGrayText = Color(0xFF424242)
    val currentUser by profileViewModel.currentUser.collectAsState()
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
                    .height(280.dp)
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
                        text = currentUser?.username ?: "Guest",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.email ?: "guest@example.com",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val imageModel: Any? = remember(currentUser?.image) {
                        val imageString = currentUser?.image
                        if (imageString.isNullOrEmpty()) {
                            null
                        } else if (imageString.startsWith("http://") || imageString.startsWith("https://")) {
                            imageString
                        } else if (imageString.startsWith("data:image/")) {
                            decodeBase64ToBitmap(imageString)
                        } else {
                            decodeBase64ToBitmap(imageString)
                        }
                    }

                    val painter = rememberAsyncImagePainter(
                        model = imageModel,
                        placeholder = painterResource(id = R.drawable.user),
                        error = painterResource(id = R.drawable.user)
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
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            // Options Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
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
                    icon = painterResource(id = R.drawable.question),
                    text = "Bantuan dan Dukungan",
                    onClick = { navController.navigate("help_support") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileOptionCard(
                    icon = Icons.Default.Info,
                    text = "Tentang Aplikasi",
                    onClick = { navController.navigate("about_app") }
                )
                Spacer(modifier = Modifier.height(24.dp))

                ProfileOptionCard(
                    icon = painterResource(id = R.drawable.logout),
                    text = "Logout",
                    onClick = { showLogoutDialog = true }
                )

            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileViewModel.logout()

                        googleSignInClient.signOut().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("GoogleSignInClient signed out successfully.")
                            } else {
                                println("Failed to sign out from GoogleSignInClient: ${task.exception?.message}")
                            }
                        }

                        navController.navigate("login") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text("Logout", color = brownColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray
        )
    }
}


@Composable
fun ProfileOptionCard(
    icon: Any,
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor = if (isPressed) Color(0xFF5D4037) else Color.White
    val textColor = if (isPressed) Color.White else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (icon is androidx.compose.ui.graphics.painter.Painter) {
                    Icon(
                        painter = icon,
                        contentDescription = text,
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}