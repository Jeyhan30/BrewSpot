package com.example.brewspot.view.cafe_detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.brewspot.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.net.URLEncoder

private val BrownColor = Color(0xFF5D4037)
private val LightGrayBackground = Color(0xFFEEEEEE)
private val WhiteBackground = Color.White
private val GrayText = Color(0xFF808080)

/**
 * Utility function to determine if a string is Base64 or a URL.
 * Returns a URL string or a Bitmap if it's Base64.
 */
fun loadImageModel(imageString: String?): Any? {
    if (imageString.isNullOrEmpty()) {
        return null
    }
    return if (imageString.startsWith("http://") || imageString.startsWith("https://") || imageString.startsWith("gs://")) {
        imageString // This is a URL or Cloud Storage URL
    } else if (imageString.startsWith("data:image/")) {
        // This is Base64 with a data URI prefix
        decodeBase64ToBitmap(imageString)
    } else {
        // Try decoding as raw Base64 without a data URI prefix
        decodeBase64ToBitmap(imageString)
    }
}

/**
 * Utility function to decode a Base64 string into a Bitmap.
 */
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
fun CafeDetailScreen(
    navController: NavController,
    cafeId: String?,
    viewModel: CafeDetailViewModel = viewModel()
) {
    val cafe by viewModel.cafe.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var userName by remember { mutableStateOf(currentUser?.username ?: "") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var totalGuests by remember { mutableStateOf(0) }
    var showGuestDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(cafeId) {
        if (cafeId != null) {
            viewModel.fetchCafeDetails(cafeId)
            viewModel.fetchCurrentUser()
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            userName = it.username
        }
    }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            selectedDate = sdf.format(calendar.time)
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        true
    )

    // Using Scaffold for basic structure and system inset handling
    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(LightGrayBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Image Section (Top Image Section)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                val topImageModel =
                    remember(cafe?.imageDetail) { loadImageModel(cafe?.imageDetail) }
                val topImagePainter = rememberAsyncImagePainter(
                    model = topImageModel,
                    placeholder = painterResource(id = R.drawable.cafeeee),
                    error = painterResource(id = R.drawable.cafeeee)
                )
                Image(
                    painter = topImagePainter,
                    contentDescription = cafe?.name ?: "Cafe Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // TopAppBar here, on top of the image
                TopAppBar(
                    title = { Text("") },
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
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }

            // Cafe Logo and Cafe Info Section (separated, overlapping with the image above)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Cafe Logo Card
                Card(
                    modifier = Modifier
                        .size(100.dp, 150.dp)
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteBackground),
                ) {
                    val smallImageModel = remember(cafe?.image) { loadImageModel(cafe?.image) }
                    val smallImagePainter = rememberAsyncImagePainter(
                        model = smallImageModel,
                        placeholder = painterResource(id = R.drawable.cafeeee),
                        error = painterResource(id = R.drawable.cafeeee)
                    )
                    Image(
                        painter = smallImagePainter,
                        contentDescription = "Cafe Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Cafe Info Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 15.dp)
                ) {
                    cafe?.let { currentCafe ->
                        Text(
                            text = currentCafe.name,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrownColor
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(id = R.drawable.location),
                                contentDescription = "Location",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentCafe.address, fontSize = 14.sp, color = BrownColor)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(id = R.drawable.clock),
                                contentDescription = "Operating Hours",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentCafe.jamOperasional, fontSize = 14.sp, color = BrownColor)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(id = R.drawable.money),
                                contentDescription = "Price Range",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rp ${currentCafe.priceRange}", fontSize = 14.sp, color = BrownColor)
                        }
                    } ?: run {
                        Text(
                            "Memuat Detail Kafe...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Reservation Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteBackground)
                    .offset(y = (-30).dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            WhiteBackground,
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        "Reservasi Sekarang!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Nama Anda", fontSize = 14.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { /* Make this field read-only */ },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = GrayText,
                            unfocusedLabelColor = GrayText,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tanggal", fontSize = 14.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = selectedDate,
                                onValueChange = { selectedDate = it },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() },
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(
                                            painterResource(id = R.drawable.calendar),
                                            contentDescription = "Calendar",
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.LightGray,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = GrayText,
                                    unfocusedLabelColor = GrayText,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Tamu", fontSize = 14.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = if (totalGuests > 0) totalGuests.toString() else "Tambah tamu",
                                onValueChange = { /* Not directly editable */ },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showGuestDialog = true },
                                trailingIcon = {
                                    IconButton(onClick = { showGuestDialog = true }) {
                                        Icon(
                                            painterResource(id = R.drawable.down),
                                            contentDescription = "Dropdown",
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.LightGray,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = GrayText,
                                    unfocusedLabelColor = GrayText,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Waktu", fontSize = 14.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = { selectedTime = it },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { timePickerDialog.show() },
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Icon(
                                    painterResource(id = R.drawable.down),
                                    contentDescription = "Dropdown",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Gray
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = GrayText,
                            unfocusedLabelColor = GrayText,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (userName.isBlank() || selectedDate.isBlank() || selectedTime.isBlank() || totalGuests == 0) {
                                Toast.makeText(
                                    context,
                                    "Harap lengkapi semua data reservasi.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                cafe?.let { currentCafe ->
                                    val encodedUserName = URLEncoder.encode(userName, "UTF-8")
                                    val encodedDate = URLEncoder.encode(selectedDate, "UTF-8")
                                    val encodedTime = URLEncoder.encode(selectedTime, "UTF-8")

                                    navController.navigate(
                                        "tableLayout/${currentCafe.id}?" +
                                                "userName=$encodedUserName&" +
                                                "date=$encodedDate&" +
                                                "time=$encodedTime&" +
                                                "totalGuests=$totalGuests"
                                    )
                                } ?: run {
                                    Toast.makeText(
                                        context,
                                        "Gagal mendapatkan detail kafe.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrownColor),
                    ) {
                        Text(
                            "Selanjutnya",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Guest Selection Dialog
            if (showGuestDialog) {
                AlertDialog(
                    onDismissRequest = { showGuestDialog = false },
                    title = { Text("Pilih Jumlah Tamu") },
                    text = {
                        Column {
                            (1..10).forEach { guestCount ->
                                TextButton(
                                    onClick = {
                                        totalGuests = guestCount
                                        showGuestDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("$guestCount Tamu")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showGuestDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}