package com.example.brewspot.view.reservationTest

// Necessary imports for Android and Compose UI
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // For back icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Import clip modifier
import androidx.compose.ui.draw.clipToBounds // Important for limiting image and overlay
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // For loading images from drawable
import androidx.compose.ui.tooling.preview.Preview // For preview in Android Studio
import androidx.compose.ui.unit.IntOffset // Required for "AREA LUAR" Text offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController // For NavController preview
import androidx.compose.foundation.rememberScrollState // For main Column scroll
import androidx.compose.foundation.verticalScroll // For main Column scroll
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel // Import for ViewModel in Compose
import android.widget.Toast // For Toast messages
import androidx.compose.ui.platform.LocalContext // For Toast messages

import androidx.compose.material3.FabPosition // IMPORTANT: Add this import!
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter

import com.example.brewspot.R // Make sure this points to your correct R file
import com.example.brewspot.view.cafe_detail.loadImageModel
import kotlinx.coroutines.flow.distinctUntilChanged

// Custom shape for the top brown background (now starting from top and curving down)
class CustomTopShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {
        val width = size.width
        val height = size.height // This is the height of the Box (e.g., 80.dp)

        // Start from top-left
        moveTo(0f, 0f)
        // Line to top-right
        lineTo(width, 0f)
        // Line to bottom-right, then curve to bottom-left
        // Adjusted values for a visible curve within the 80.dp height
        // The Y-coordinate for the end points should be slightly less than 'height'
        // The control point Y-coordinate should be greater than the end points for a curve
        lineTo(width, height * 0.75f) // End point Y (e.g., 80 * 0.75 = 60dp)
        quadraticBezierTo(
            width / 2, height * 1.1f, // Control point Y for a deeper curve (e.g., 80 * 1.1 = 88dp)
            0f, height * 0.75f // End point Y
        )
        // Close the path back to top-left
        close()
    })
}


// =============================================================================
// Composable: TableItem (Visual Representation of Table)
// =============================================================================
@Composable
fun TableItem(
    tableId: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Determine background color based on table status
    val backgroundColor = when {
        isBooked -> Color(0xFFE53935) // Red for 'Booked table'
        isSelected -> Color.DarkGray // Dark gray for 'Selected table'
        else -> Color.White // White for 'Empty table'
    }
    // Determine border color
    val borderColor = if (isBooked || isSelected) backgroundColor else Color.Gray
    // Determine text color
    val textColor = when {
        isBooked -> Color.White
        isSelected -> Color.White
        else -> Color.Black
    }

    // Box is the basic container for the table item
    Box(
        modifier = Modifier
            .size(48.dp) // **Reduced from 60.dp to 48.dp**
            .background(backgroundColor, RoundedCornerShape(8.dp)) // Background with rounded corners
            .clickable(enabled = !isBooked) { onClick() } // Clickable if not booked
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)), // Border
        contentAlignment = Alignment.Center // Center content inside the Box
    ) {
        // Display table ID inside the box
        Text(text = tableId, color = textColor, fontSize = 12.sp) // **Font size also adjusted**
    }
}

// =============================================================================
// Composable: LegendItem (Item for Legend/Description)
// =============================================================================
@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableLayoutScreen(
    navController: NavController,
    cafeId: String?,
    userName: String?,
    date: String?,
    time: String?,
    totalGuests: Int,
    viewModel: TableViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current // Dapatkan LifecycleOwner

    val tables by viewModel.tables.collectAsState()
    val selectedTables = viewModel.selectedTables
    val cafe by viewModel.cafe.collectAsState() // Ini mengumpulkan state Cafe dari ViewModel

    val primaryBrown = Color(0xFF4E342E)
    val lighterGrey = Color(0xFFF0F0F0)

    val scrollState = rememberScrollState()
    val fabVisible = remember { mutableStateOf(true) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress } // Amati isScrollInProgress
            .distinctUntilChanged() // Hanya bereaksi jika nilai berubah (true -> false atau false -> true)
            .collect { isScrolling ->
                if (isScrolling) {
                    fabVisible.value = false // Sembunyikan saat sedang menggulir
                } else {
                    // Ketika guliran berhenti, tampilkan FAB
                    fabVisible.value = true
                }
            }
    }


    // Panggil setReservationDetails saat argumen berubah
    LaunchedEffect(cafeId, userName, date, time, totalGuests) {
        viewModel.setReservationDetails(cafeId, userName, date, time, totalGuests)
        // refreshTableStatusIfCafeIdSet akan dipanggil di DisposableEffect saat ON_RESUME
        // Atau secara eksplisit di sini jika memang ingin di-refresh saat argumen berubah,
        // tapi DisposableEffect akan lebih baik untuk kasus resume layar.
    }

    // SOLUSI UTAMA: Menggunakan DisposableEffect dengan LifecycleObserver
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Saat layar resume (kembali dari layar lain seperti ConfirmationScreen)
                viewModel.refreshTableStatusIfCafeIdSet()
                println("TableLayoutScreen: ON_RESUME, refreshing table status.")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pemilihan Meja",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryBrown)
            )
        },
        floatingActionButton = {
            val selectedTableCount = selectedTables.size
            val canProceed = selectedTableCount > 0

            val fabContainerColor = if (canProceed) primaryBrown else primaryBrown.copy(alpha = 0.5f)
            val fabContentColor = if (canProceed) Color.White else Color.White.copy(alpha = 0.7f)

            val onClickAction: (() -> Unit)? = if (canProceed) {
                {
                    if (cafeId != null && userName != null && date != null && time != null) {
                        viewModel.createReservation(
                            cafeId = cafeId,
                            cafeName = cafe?.name ?: "Unknown Cafe", // PERBAIKAN DI SINI: Gunakan 'cafe?.name'
                            userName = userName,
                            date = date,
                            totalGuests = totalGuests,
                            time = time,
                            onSuccess = { reservationId ->
                                Toast.makeText(context, "Reservasi berhasil! Silakan pilih menu.", Toast.LENGTH_SHORT).show()
                                navController.navigate("menu/${cafeId}?reservationId=${reservationId}") {
                                    popUpTo("welcome") {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(context, "Reservasi gagal: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Data reservasi tidak lengkap.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                null
            }

            AnimatedVisibility(
                visible = fabVisible.value,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    onClick = onClickAction ?: {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = fabContainerColor,
                    shape = RoundedCornerShape(8.dp),
                    contentColor = fabContentColor
                ) {
                    Text("Selanjutnya (${selectedTableCount} Meja Dipilih)", color = Color.White, fontSize = 18.sp)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(lighterGrey)
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(scrollState)
        ) {
            // Area for the cafe layout image (as background visual only)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp) // Fixed height for the layout area
                    .padding(horizontal = 16.dp) // Add horizontal padding
                    .background(Color(0xFFF5F5DC), RoundedCornerShape(16.dp))
                    .clipToBounds()
            ) {
                val denahImageModel = remember(cafe?.denahImage) { loadImageModel(cafe?.denahImage) }
                val denahPainter = rememberAsyncImagePainter(
                    model = denahImageModel,
                    placeholder = painterResource(id = R.drawable.cafeeee), // Default placeholder
                    error = painterResource(id = R.drawable.cafeeee) // Error placeholder
                )

                Image(
                    painter = denahPainter, // Use the painter with the denah image
                    contentDescription = "Denah Cafe",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Keterangan:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                LegendItem(color = Color.White, text = "Meja kosong")
                Spacer(modifier = Modifier.height(4.dp))
                LegendItem(color = Color.DarkGray, text = "Meja yang dipilih")
                Spacer(modifier = Modifier.height(4.dp))
                LegendItem(color = Color(0xFFE53935), text = "Meja terisi")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title for scrollable table list
            Text(
                "Pilih Meja Anda:",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Table List
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredAndSortedTables = tables.entries
                    .filter { (id, _) -> id != "PHOTO BOOTH" && id != "KASIR" && id != "TEMPAT PARKIR" }
                    .sortedBy { it.key.replace("T", "").toIntOrNull() ?: Int.MAX_VALUE }

                items(filteredAndSortedTables) { (id, isBooked) ->
                    TableItem(
                        tableId = id,
                        isBooked = isBooked,
                        isSelected = viewModel.selectedTables.contains(id),
                        onClick = {
                            if (!isBooked) {
                                viewModel.toggleTableSelection(id)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}