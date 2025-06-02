package com.example.brewspot.view.profile


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.brewspot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPasswordVisible by remember { mutableStateOf(false) }

    val brownColor = Color(0xFF5D4037)
    val lightGrayBorder = Color(0xFFE0E0E0)
    val lightBrownBackground = Color(0xFFF0F0F0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ganti Kata Sandi",
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Kata Sandi Lama
                Text(
                    "Kata Sandi Lama",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(painterResource(id = R.drawable.lock), contentDescription = "Lock Icon", modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        val image = if (oldPasswordVisible)
                            painterResource(id = R.drawable.show)
                        else
                            painterResource(id = R.drawable.hide)
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                painter = image,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
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

                Text(
                    "Kata Sandi Baru",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(painterResource(id = R.drawable.lock), contentDescription = "Lock Icon", modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        val image = if (newPasswordVisible)
                            painterResource(id = R.drawable.show)
                        else
                            painterResource(id = R.drawable.hide)
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                painter = image,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
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

                Text(
                    "Konfirmasi Kata Sandi",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(painterResource(id = R.drawable.lock), contentDescription = "Lock Icon", modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        val image = if (confirmNewPasswordVisible)
                            painterResource(id = R.drawable.show)
                        else
                            painterResource(id = R.drawable.hide)
                        IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                            Icon(
                                painter = image,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
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
                    if (oldPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank()) {
                        Toast.makeText(context, "Semua kolom kata sandi harus diisi.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPassword.length < 6) {
                        Toast.makeText(context, "Kata Sandi Baru minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPassword != confirmNewPassword) {
                        Toast.makeText(context, "Kata Sandi Baru dan Konfirmasi Kata Sandi tidak cocok.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    profileViewModel.changePassword(
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        onSuccess = {
                            Toast.makeText(context, "Kata Sandi berhasil diganti!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(context, "Gagal mengganti kata sandi: $errorMessage", Toast.LENGTH_LONG).show()
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