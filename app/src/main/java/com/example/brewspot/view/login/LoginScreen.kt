package com.example.brewspot.view.login

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brewspot.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Corrected Custom shape for the top brown background
class CustomTopShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(Path().apply {
        val width = size.width
        val height = size.height

        moveTo(0f, 0f)
        lineTo(width, 0f)
        lineTo(width, height * 0.65f)
        quadraticBezierTo(
            width / 2, height * 0.95f,
            0f, height * 0.65f
        )
        close()
    })
}



@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val brownColor = Color(0xFF5D4037)
    val orangeColor = Color(0xFFB37300)
    val lightGrayBackground = Color(0xFFE0E0E0)
    val darkGrayText = Color(0xFF424242)

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
                                            navController.navigate("welcome/${user?.displayName ?: "User"}") {
                                                popUpTo("login") { inclusive = true }
                                            }                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Gagal menyimpan user: ${e.localizedMessage}"
                                        }
                                } else {
                                    navController.navigate("welcome/${it.displayName ?: "User"}") {
                                        popUpTo("login") { inclusive = true }
                                    }                                }
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
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .clip(CustomTopShape())
                .fillMaxWidth()
                .weight(0.3f)
                .background(brownColor)
                ,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 0.dp)
            ) {
                Text(
                    "Login",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Masukkan email dan password yang telah Anda\ndaftarkan untuk masuk ke aplikasi",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .padding(horizontal = 24.dp)
                .offset(y = (-40).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(painterResource(id = R.drawable.email), contentDescription = "Email Icon", modifier = Modifier.size(20.dp)) },
                        shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(painterResource(id = R.drawable.lock), contentDescription = "Password Icon", modifier = Modifier.size(20.dp)) },
                        singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.show)
                    else
                        painterResource(id = R.drawable.hide)
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = image,
                            contentDescription = "Toggle password visibility",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Spacer(modifier = Modifier.height(24.dp))

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val backgroundColor = if (isPressed) Color(0xFF5D4037) else lightGrayBackground
            val textColor = if (isPressed) Color.White else darkGrayText



            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email dan kata sandi tidak boleh kosong."
                        return@Button
                    }

                    viewModel.login(email, password) { success, username ->
                        if (success && username != null) {
                            navController.navigate("welcome/$username") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            errorMessage = "Email atau password salah. Silakan coba lagi."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                border = BorderStroke(1.dp, Color.LightGray),
                interactionSource = interactionSource
            ) {
                Text("Masuk", color = textColor, fontSize = 16.sp)
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray, thickness = 1.dp)
                Text(
                    "Atau dengan",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icons8_google_480),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Masuk dengan Google", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Belum punya akun? ", color = Color.Gray, style = MaterialTheme.typography.bodyMedium) // Adjusted color
                Text(
                    "Daftar",
                    color = orangeColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    },
                )
            }
        }
    }
}