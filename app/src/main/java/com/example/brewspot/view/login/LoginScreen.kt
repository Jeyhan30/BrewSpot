package com.example.brewspot.view.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.brewspot.R
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val orangeColor = Color(0xFFB37300)

    val context = LocalContext.current
    val activity = context as? Activity

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("46336032851-psnppj5vbnt7oq5ri5c7qn0u7q4knoj9.apps.googleusercontent.com") // Ganti dengan ID dari Firebase Console
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Firebase.auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        val user = Firebase.auth.currentUser

                        user?.let {
                            val db = Firebase.firestore
                            val userDoc = db.collection("users").document(it.uid)

                            userDoc.get().addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    // simpan data user baru ke Firestore
                                    val userData = hashMapOf(
                                        "email" to it.email,
                                        "username" to (it.displayName ?: it.email?.substringBefore('@') ?: "User")
                                    )

                                    userDoc.set(userData)
                                        .addOnSuccessListener {
                                            navController.navigate("welcome/${user?.displayName ?: "User"}")
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Gagal menyimpan user: ${e.localizedMessage}"
                                        }
                                } else {
                                    // user sudah ada, langsung lanjut
                                    navController.navigate("welcome/${it.displayName ?: "User"}")
                                }
                            }.addOnFailureListener { e ->
                                errorMessage = "Error cek user: ${e.localizedMessage}"
                            }
                        }
                    } else {
                        errorMessage = "Login Google gagal."
                    }
                }
        } catch (e: Exception) {
            errorMessage = "Google Sign-In error: ${e.localizedMessage}"
        }
    }



    // Pop-up untuk error login
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Login Gagal") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        RoundedTextField(
            value = email,
            onValueChange = { email = it },
            label = "Masukkan email Anda",
            placeholder = "email@gmail.com"
        )

        Spacer(modifier = Modifier.height(8.dp))

        com.example.brewspot.view.register.RoundedTextField(
            value = password,
            onValueChange = { password = it },
            label = "Masukkan password Anda",
            placeholder = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Lupa password Anda?",
            color = orangeColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp)
                .clickable {
                    // TODO: aksi lupa password
                },
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                viewModel.login(email, password) { success, username ->
                    if (success && username != null) {
                        navController.navigate("welcome/$username")
                    } else {
                        errorMessage = "Email atau password salah. Silakan coba lagi."
                    }
                }
            },
            border = BorderStroke(1.dp, orangeColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Masuk", color = orangeColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Belum punya akun? ", style = MaterialTheme.typography.bodySmall)
            Text(
                "Daftar sekarang!",
                color = orangeColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                },
                textDecoration = TextDecoration.Underline
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(
                "atau",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Divider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            SocialIconButton(
                iconRes = R.drawable.icons8_google_480,
                contentDescription = "Google login"
            ) {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }

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
        modifier = Modifier.fillMaxWidth(),
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
