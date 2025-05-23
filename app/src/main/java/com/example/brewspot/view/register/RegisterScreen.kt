package com.example.brewspot.view.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.brewspot.R

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showMismatchDialog by remember { mutableStateOf(false) }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState == "success") {
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    if (showMismatchDialog) {
        AlertDialog(
            onDismissRequest = { showMismatchDialog = false },
            title = { Text("Kesalahan") },
            text = { Text("Password dan konfirmasi tidak cocok.") },
            confirmButton = {
                TextButton(onClick = { showMismatchDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RoundedTextField(
            value = email,
            onValueChange = { email = it },
            label = "Masukkan email Anda",
            placeholder = "email@gmail.com"
        )

        Spacer(modifier = Modifier.height(12.dp))

        RoundedTextField(
            value = username,
            onValueChange = { username = it },
            label = "Masukkan username Anda",
            placeholder = "Username"
        )

        Spacer(modifier = Modifier.height(12.dp))

        RoundedTextField(
            value = password,
            onValueChange = { password = it },
            label = "Masukkan password Anda",
            placeholder = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        RoundedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Masukkan kembali password Anda",
            placeholder = "Re-Type Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                if (password == confirmPassword) {
                    viewModel.register(email, password, username)
                } else {
                    showMismatchDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, Color(0xFFDA8000))
        ) {
            Text("Daftar", color = Color(0xFFDA8000))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("Sudah punya akun?")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Masuk sekarang!",
                color = Color(0xFFDA8000),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Divider(modifier = Modifier.weight(1f))
            Text("  atau  ", style = MaterialTheme.typography.bodySmall)
            Divider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SocialIconButton(
                iconRes = R.drawable.icons8_google_480,
                contentDescription = "Google login"
            ) {
                // TODO: login Google
            }        }

        registerState?.takeIf { it != "success" }?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun RoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
    )
}

@Composable
fun SocialIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.LightGray),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Unspecified)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}