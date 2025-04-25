package com.example.lifetracker.ui.screens.dashboard

import android.annotation.SuppressLint
import android.util.Log
import android.content.ActivityNotFoundException
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.lifetracker.ui.components.AddMetricButton
import com.example.lifetracker.ui.components.AddMetricPopup
import com.example.lifetracker.ui.components.ClickableMetricCardWithChart
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty as listIsNotEmpty

@SuppressLint("DefaultLocale")
@Composable
fun DashboardScreen(
    onNavigateToEditMetric: (String) -> Unit,
    onNavigateToViewBMIHistory: () -> Unit,
    viewModel: HealthViewModel,
    navController: NavController
) {
    // State for showing the popup
    var showAddMetricPopup by remember { mutableStateOf(false) }
    
    // Context for launching intents
    val context = LocalContext.current
    
    // Get latest values from history
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Get history data for charts
    val weightHistory = viewModel.getMetricHistory("Weight", "kg")
    val heightHistory = viewModel.getMetricHistory("Height", "cm")
    val bodyFatHistory = viewModel.getMetricHistory("Body Fat", "%")
    val bmiHistory = viewModel.getMetricHistory("BMI", "")

    // Collect step count from Health Connect
    val stepCount = viewModel.stepCount.collectAsStateWithLifecycle().value
    
    // Permission launcher with delayed status check
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check permission status after returning from the permission screen
        Log.d("DashboardScreen", "Permission request completed, checking status")
        
        // Wait a moment before checking again to allow system to update permission status
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.checkHealthConnectStatus()
        }, 1000)
    }
    
    // Function to handle permission request with better fallbacks
    val requestPermission = {
        try {
            // First try the direct API method
            viewModel.requestHealthConnectPermissions()
            
            // Then use the intent approach as backup after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (!viewModel.permissionsGranted) {
                    val intent = viewModel.getPermissionRequestIntent()
                    if (intent != null) {
                        try {
                            permissionLauncher.launch(intent)
                        } catch (e: Exception) {
                            Log.e("DashboardScreen", "Failed to launch permission intent", e)
                            Toast.makeText(context, "Could not request Health Connect permissions. Please grant them manually.", Toast.LENGTH_LONG).show()
                            
                            // As a last resort, open Health Connect app
                            try {
                                context.startActivity(viewModel.getHealthConnectAppIntent())
                            } catch (e2: Exception) {
                                Log.e("DashboardScreen", "Failed to open Health Connect app", e2)
                            }
                        }
                    }
                }
            }, 500) // Short delay to give direct API a chance first
        } catch (e: Exception) {
            Log.e("DashboardScreen", "Error in permission flow", e)
            Toast.makeText(context, "Error accessing Health Connect", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Check status when screen appears and after orientation changes
    LaunchedEffect(Unit) {
        Log.d("DashboardScreen", "Initial Health Connect status check")
        viewModel.checkHealthConnectStatus()
        
        // Give Health Connect a moment to register permissions
        Handler(Looper.getMainLooper()).postDelayed({
            if (viewModel.permissionsGranted) {
                viewModel.refreshStepData()
            }
        }, 1000)
    }

    // Format values based on history
    val formattedWeight = if (weightHistory.size > 0) {
        val value = latestWeight ?: 0f
        if (value > 0) {
            val formatted = String.format("%.1f", value)
            if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
        } else "No Data"
    } else "No Data"
    
    val formattedHeight = if (heightHistory.size > 0) {
        val value = latestHeight ?: 0f
        if (value > 0) value.toInt().toString() else "No Data"
    } else "No Data"
    
    val formattedBodyFat = if (bodyFatHistory.size > 0) {
        val value = latestBodyFat ?: 0f
        if (value > 0) {
            val formatted = String.format("%.1f", value)
            if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
        } else "No Data"
    } else "No Data"

    // Calculate BMI with null safety
    val bmi = if (weightHistory.size > 0 && heightHistory.size > 0 && 
                 latestWeight != null && latestHeight != null && 
                 latestWeight > 0 && latestHeight > 0) {
        calculateBMI(latestWeight, latestHeight)
    } else {
        0f
    }

    val formattedBmi = if (bmi > 0) {
        val formatted = String.format("%.1f", bmi)
        if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
    } else "No Data"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header with title and add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                AddMetricButton(
                    onClick = { showAddMetricPopup = true }
                )
            }

            // Step counter card
            Text(
                text = "TODAY'S ACTIVITY",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            
            // Card showing step information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Steps",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            if (viewModel.healthConnectAvailable) {
                                if (viewModel.permissionsGranted) {
                                    // Show step count
                                    Text(
                                        text = "$stepCount",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // Add a refresh button
                                    IconButton(onClick = { viewModel.refreshStepData() }) {
                                        Icon(
                                            // Use FontAwesome or other icon here
                                            // e.g., FontAwesomeIcon(icon = FontAwesomeIcons.Sync)
                                            // or Material icon
                                            Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = Color.White
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Connect to Health Data",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Button(
                                            onClick = { requestPermission() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF2196F3)
                                            ),
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            Text("Grant Access", color = Color.White)
                                        }
                                    }
                                }
                            } else {
                                // Health Connect not available
                                Text(
                                    text = "Health Connect not available",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Button(
                                    onClick = { 
                                        context.startActivity(viewModel.getHealthConnectInstallIntent())
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    )
                                ) {
                                    Text("Install Health Connect", color = Color.White)
                                }
                            }
                        }
                        
                        // Replace DirectionsWalk with FontAwesome icon
                        FaIcon(
                            faIcon = FaIcons.Running,
                            tint = Color(0xFF2196F3),
                            size = 32.dp
                        )
                    }
                }
            }

            // BODY METRICS header
            Text(
                text = "BODY METRICS",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )

            // Metric cards grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ClickableMetricCardWithChart(
                        title = "Weight",
                        value = formattedWeight,
                        unit = "kg",
                        history = weightHistory,
                        onClick = { onNavigateToEditMetric("Weight") },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCardWithChart(
                        title = "Height",
                        value = formattedHeight,
                        unit = "cm",
                        history = heightHistory,
                        onClick = { onNavigateToEditMetric("Height") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ClickableMetricCardWithChart(
                        title = "BMI",
                        value = formattedBmi,
                        unit = "",
                        history = bmiHistory,
                        onClick = { onNavigateToViewBMIHistory() },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCardWithChart(
                        title = "Body Fat",
                        value = formattedBodyFat,
                        unit = "%",
                        history = bodyFatHistory,
                        onClick = { onNavigateToEditMetric("Body Fat") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    // Show the popup when showAddMetricPopup is true
    if (showAddMetricPopup) {
        AddMetricPopup(
            onDismiss = { showAddMetricPopup = false },
            onNavigateToEditMetric = onNavigateToEditMetric,
            navController = navController
        )
    }
}
