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

import com.example.brewspot.R // Make sure this points to your correct R file

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

// =============================================================================
// Composable: TableLayoutScreen (Main Screen)
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class) // For TopAppBarDefaults
@Composable
fun TableLayoutScreen(
    navController: NavController,
    cafeId: String?,
    userName: String?,
    date: String?,
    time: String?,
    totalGuests: Int, // Receive totalGuests here
    viewModel: TableViewModel = viewModel()
) {
    val context = LocalContext.current // Get context for Toast messages

    // Observe table state from ViewModel
    val tables by viewModel.tables.collectAsState()
    // State for currently selected tables (now stored in ViewModel as a set)
    val selectedTables = viewModel.selectedTables

    // Custom colors from image
    val primaryBrown = Color(0xFF4E342E) // Dark brown for TopAppBar and buttons
    val lighterGrey = Color(0xFFF0F0F0) // Light gray color for main background

    // Scroll state for main Column
    val scrollState = rememberScrollState()

    // State to control FAB visibility
    val fabVisible = remember { mutableStateOf(true) }

    // Side effect to monitor scroll state
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value } // Observe scroll position changes
            .collect { scrollPosition ->
                if (scrollState.isScrollInProgress) {
                    // If scrolling, hide FAB
                    if (fabVisible.value) fabVisible.value = false
                } else {
                    // If not scrolling, show FAB (after a short delay to ensure scrolling has truly stopped)
                    if (!fabVisible.value) fabVisible.value = true
                }
            }
    }

    // Pass reservation details to ViewModel
    LaunchedEffect(cafeId, userName, date, time, totalGuests) {
        viewModel.setReservationDetails(cafeId, userName, date, time, totalGuests)
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
            val canProceed = selectedTableCount == totalGuests && totalGuests > 0

            val fabContainerColor = if (canProceed) primaryBrown else primaryBrown.copy(alpha = 0.5f) // Dimmer when disabled
            val fabContentColor = if (canProceed) Color.White else Color.White.copy(alpha = 0.7f) // Dimmer text when disabled

            val onClickAction: (() -> Unit)? = if (canProceed) {
                {
                    viewModel.bookSelectedTables() // Book all selected tables
                    // Create reservation in Firestore
                    if (cafeId != null && userName != null && date != null && time != null) {
                        viewModel.createReservation(
                            cafeId = cafeId,
                            cafeName = viewModel.cafe?.name ?: "Unknown Cafe", // Get cafe name from ViewModel
                            userName = userName,
                            date = date,
                            totalGuests = totalGuests,
                            time = time,
                            onSuccess = {
                                Toast.makeText(context, "Reservasi berhasil!", Toast.LENGTH_SHORT).show()
                                navController.navigate("confirmation_screen") { // Navigate to confirmation screen or home
                                    popUpTo("home") { inclusive = false } // Pop up to home screen, not inclusive
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
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), // Slide from bottom & fade in
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(), // Slide to bottom & fade out
            ) {
                ExtendedFloatingActionButton(
                    onClick = onClickAction ?: {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = fabContainerColor, // Apply dynamic color
                    shape = RoundedCornerShape(8.dp),
                    contentColor = fabContentColor // Apply dynamic content color
                ) {
                    Text("Selanjutnya (${selectedTableCount}/$totalGuests)", color = Color.White, fontSize = 18.sp)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        // Main screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(lighterGrey)
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(scrollState)
        ) {
            // CustomTopShape as a curved background below the TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .offset(y = (-1).dp)
                    .clip(CustomTopShape())
                    .background(primaryBrown)
            ) {
                // This Box itself doesn't contain content visible in the image, it's just the shape.
            }

            // --- Start of "Pilih Meja" section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lighterGrey) // Ensure it has its own background
                    .padding(horizontal = 16.dp) // Add horizontal padding to this section
            ) {
                Text(
                    "Pilih Meja",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Text(
                    "Klik kotak putih untuk memilih meja Anda!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp)) // Spacer after the text
            }
            // --- End of "Pilih Meja" section ---

            // Area for the cafe layout image (as background visual only)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp) // Fixed height for the layout area
                    .padding(horizontal = 16.dp) // Add horizontal padding
                    .background(Color(0xFFF5F5DC), RoundedCornerShape(16.dp))
                    .clipToBounds()
            ) {
                // Cafe layout image as background
                Image(
                    painter = painterResource(id = R.drawable.cafeeee), // <-- Replace with your file name!
                    contentDescription = "Denah Cafe",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Adjust ContentScale if needed
                )

                // "AREA LUAR" text on top of the layout image
                Text("AREA LUAR",
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Position at top-right of the Box
                        .offset(x = (-16).dp, y = 16.dp), // Adjust offset from top-right
                    color = Color.Gray,
                    fontSize = 12.sp
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
                // Filter out special "tables" that aren't actual seating tables
                val filteredAndSortedTables = tables.entries
                    .filter { (id, _) -> id != "PHOTO BOOTH" && id != "KASIR" && id != "TEMPAT PARKIR" }
                    .sortedBy { it.key.replace("T", "").toIntOrNull() ?: Int.MAX_VALUE }

                items(filteredAndSortedTables) { (id, isBooked) ->
                    TableItem(
                        tableId = id,
                        isBooked = isBooked,
                        isSelected = viewModel.selectedTables.contains(id), // Check if table is in the set of selected tables
                        onClick = {
                            if (!isBooked) {
                                viewModel.toggleTableSelection(id, totalGuests) // Pass totalGuests to toggle logic
                            }
                        }
                    )
                }
            }
            // Add spacer at the very bottom so content is not cut off by FAB
            Spacer(modifier = Modifier.height(80.dp)) // Spacer height roughly FAB height + padding
        }
    }
}