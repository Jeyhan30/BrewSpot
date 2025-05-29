package com.example.brewspot.view.register

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.brewspot.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Define CustomTopShape here or ensure it's imported from a common location.
// For this example, I'll put it here assuming it's meant to be shared.
// If it's exclusively in LoginScreen, you'd import it.
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
        lineTo(width, height * 0.7f)
        quadraticBezierTo(
            width / 2, height * 1f,
            0f, height * 0.7f
        )
        close()
    })
}


@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMismatchDialog by remember { mutableStateOf(false) }

    // Define colors consistently with LoginScreen
    val brownColor = Color(0xFF5D4037)
    val orangeColor = Color(0xFFB37300)
    val lightGrayBackground = Color(0xFFE0E0E0)
    val darkGrayText = Color(0xFF424242)

    val context = LocalContext.current
    val activity = context as? Activity

    // --- Google Sign-In related code (copied from LoginScreen) ---
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("46336032851-psnppj5vbnt7oq5ri5c7qn0u7q4knoj9.apps.googleusercontent.com") // Ganti dengan ID dari Firebase Console
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
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
                        errorMessage = "Pendaftaran Google gagal." // Changed message for registration
                    }
                }
        } catch (e: Exception) {
            errorMessage = "Google Sign-Up error: ${e.localizedMessage}" // Changed message for registration
        }
    }
    // --- End Google Sign-In related code ---

    LaunchedEffect(viewModel.registerState.collectAsState().value) {
        val state = viewModel.registerState.value
        when (state) {
            "success" -> {
                navController.navigate("login") { // Navigate to login after successful traditional registration
                    popUpTo("register") { inclusive = true }
                }
            }
            "loading" -> { /* Optionally show a loading indicator */ }
            "idle" -> { /* Do nothing */ }
            else -> {
                errorMessage = state // Set error message from ViewModel for traditional registration errors
            }
        }
    }

    // Pop-up for error or warning messages
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Peringatan") }, // Changed title to "Peringatan"
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
    // --- End Google Sign-In related code ---

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top section
        Box(
            modifier = Modifier
                .clip(CustomTopShape())
                .fillMaxWidth()
                .weight(0.3f)
                .background(brownColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 0.dp) // Adjusted top padding for content
            ) {
                Text(
                    "Registrasi", // Matches image
                    color = Color.White,
                    fontSize = 32.sp, // Matches image
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Isi form di bawah ini terlebih dahulu sebelum\nmemulai aplikasi", // Matches image
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Bottom section (register form)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .padding(horizontal = 24.dp)
                .offset(y = (-40).dp), // Overlap with top section
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between fields
        ) {
            // Spacer to adjust vertical position relative to the curve
            Spacer(modifier = Modifier.height(0.dp))

            // Username Field (first as per image)
            RoundedTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                placeholder = "Username",
                leadingIcon = { Icon(painterResource(id = R.drawable.user), contentDescription = "Username Icon", modifier = Modifier.size(20.dp)) }
            )

            // Email Field (second as per image)
            RoundedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "email@gmail.com",
                keyboardType = KeyboardType.Email,
                leadingIcon = { Icon(painterResource(id = R.drawable.email), contentDescription = "Email Icon", modifier = Modifier.size(20.dp)) }
            )

            // Password Field (third as per image)
            RoundedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Kata Sandi",
                placeholder = "Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                leadingIcon = { Icon(painterResource(id = R.drawable.lock), contentDescription = "Password Icon", modifier = Modifier.size(20.dp)) }
            )

            // Confirm Password Field (fourth as per image)
            RoundedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Kata Sandi",
                placeholder = "Re-Type Password",
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                leadingIcon = { Icon(painterResource(id = R.drawable.lock), contentDescription = "Confirm Password Icon", modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(1.dp)) // Increased spacing before button as per image

            val registerButtonInteractionSource = remember { MutableInteractionSource() }
            val isRegisterButtonPressed by registerButtonInteractionSource.collectIsPressedAsState()

            val registerButtonBackgroundColor = if (isRegisterButtonPressed) brownColor else lightGrayBackground
            val registerButtonTextColor = if (isRegisterButtonPressed) Color.White else darkGrayText

            // "Daftar" Button
            Button(
                onClick = {
                    if (password == confirmPassword) {
                        viewModel.register(email, password, username)
                    } else {
                        showMismatchDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = registerButtonBackgroundColor),
                border = BorderStroke(1.dp, Color.LightGray),
                interactionSource = registerButtonInteractionSource
            ) {
                Text("Daftar", color = registerButtonTextColor, fontSize = 16.sp)
            }


            // "Daftar dengan Google" Button
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
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White, // White background as per image
                    contentColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icons8_google_480),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daftar dengan Google", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Sudah mempunyai akun? ", // Matches image
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Masuk", // Matches image
                    color = orangeColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        navController.navigate("login")                    },
                    textDecoration = TextDecoration.None // Explicitly no underline as per image
                )
            }
        }
    }
}

// Re-using and slightly modifying the RoundedTextField for leadingIcon support
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null // Added leadingIcon parameter
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = leadingIcon, // Applied leading icon
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible)
                    painterResource(id = R.drawable.show) // Make sure you have 'show.xml' drawable
                else
                    painterResource(id = R.drawable.hide) // Make sure you have 'hide.xml' drawable
                IconButton(onClick = { onPasswordVisibilityToggle?.invoke() }) {
                    Icon(
                        painter = image,
                        contentDescription = "Toggle password visibility",
                        modifier = Modifier.size(20.dp) // Consistent size
                    )
                }
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
}

// This SocialIconButton is no longer directly used for the Google button in RegisterScreen
// as it's now an OutlinedButton, but keeping it if it's used elsewhere in your project.
@Composable
fun SocialIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Daftar dengan Google", fontSize = 16.sp)
    }
}