package com.example.brewspot.view.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType // <--- Ensure this is imported
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R
import com.example.brewspot.view.profile.ProfileViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val currentUser by profileViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // State for editable fields, initialized with current user data
    var name by remember { mutableStateOf(currentUser?.username ?: "") }
    var phoneNumber by remember { mutableStateOf(currentUser?.phoneNumber ?: "") } // <--- CHANGED: email replaced with phoneNumber
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    // State untuk Base64 string dari gambar profil yang sedang ditampilkan atau yang baru dipilih
    var displayedImageBase64 by remember { mutableStateOf<String?>(null) }
    // Update state when currentUser changes (e.g., after fetching data or saving)
    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.username
            phoneNumber = it.phoneNumber
            // Jika currentUser memiliki imageUrl (Base64), tampilkan
            if (it.image.isNotEmpty()) {
                displayedImageBase64 = it.image
            } else {
                displayedImageBase64 = null
            }
            selectedImageUri = null // Reset URI setelah data user di-load/update
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Simpan URI sementara untuk menampilkan preview
                selectedImageUri = it
                // Konversi Uri ke Base64 string
                val base64String = profileViewModel.convertUriToBase64(context, it)
                if (base64String != null) {
                    displayedImageBase64 = base64String // Update gambar yang ditampilkan segera
                    // Langsung update profil user dengan Base64 gambar
                    profileViewModel.updateProfile(
                        username = name,
                        phoneNumber = phoneNumber,
                        base64Image = base64String, // Teruskan Base64 string ke ViewModel
                        onSuccess = {
                            Toast.makeText(context, "Profil dan foto berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(context, "Gagal memperbarui foto: $errorMessage", Toast.LENGTH_LONG).show()
                            displayedImageBase64 = currentUser?.image // Kembalikan ke gambar sebelumnya jika gagal
                        }
                    )
                } else {
                    Toast.makeText(context, "Gagal mengkonversi gambar.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val brownColor = Color(0xFF5D4037) // Dark brown color
    val lightGrayBorder = Color(0xFFE0E0E0) // Light gray for text field borders
    val lightBrownBackground = Color(0xFFF0F0F0) // Light gray for overall background

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profil",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = brownColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(lightBrownBackground),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture with Edit Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { imagePickerLauncher.launch("image/*") }, // KLIK UNTUK MEMILIH GAMBAR
                contentAlignment = Alignment.Center
            ) {
                // Menentukan gambar yang akan ditampilkan
                val imageBitmap: Bitmap? = remember(displayedImageBase64) {
                    decodeBase64ToBitmap(displayedImageBase64)
                }

                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap.asImageBitmap(), // Gunakan asImageBitmap untuk Bitmap
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder jika tidak ada gambar atau gagal decode
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Default Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Edit Icon Overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(brownColor)
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .clickable { imagePickerLauncher.launch("image/*") }, // KLIK UNTUK MEMILIH GAMBAR
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Nama
                Text(
                    "Nama",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brownColor,
                        unfocusedBorderColor = lightGrayBorder,
                        focusedLabelColor = brownColor,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = brownColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Nomor Telepon (formerly Email)
                Text(
                    "Nomor Telepon", // <--- CHANGED: Label for phone number
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = phoneNumber, // <--- CHANGED: Use phoneNumber state
                    onValueChange = { phoneNumber = it }, // <--- CHANGED: Update phoneNumber state
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brownColor,
                        unfocusedBorderColor = lightGrayBorder,
                        focusedLabelColor = brownColor,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = brownColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Simpan Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // Validation for phone number (optional, but good practice)
                    // You might want more sophisticated phone number validation
                    if (phoneNumber.isBlank()) {
                        Toast.makeText(context, "Nomor Telepon tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    profileViewModel.updateProfile(
                        username = name,
                        phoneNumber = phoneNumber, // <--- CHANGED: Pass phoneNumber
                        onSuccess = {
                            Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack() // Go back to profile screen
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(context, "Gagal memperbarui profil: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brownColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Simpan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}