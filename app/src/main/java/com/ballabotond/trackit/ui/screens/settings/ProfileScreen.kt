package com.ballabotond.trackit.ui.screens.settings

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    authViewModel: com.ballabotond.trackit.ui.viewmodel.AuthViewModel
) {
    val context = LocalContext.current
    
    var showExportDialog by remember { mutableStateOf(false) }

    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }

    val buildNumber = try {
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toString()
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }

    // File saver for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { exportMetricsToCsv(context, it, viewModel) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                // Header with consistent styling
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Close button
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF333333),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                // Appearance Section
                SectionHeader("Appearance")
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Star,
                    iconColor = Color(0xFF4CAF50),
                    title = "Theme",
                    onClick = { /* Theme implementation not required */ }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Settings,
                    iconColor = Color(0xFF4CAF50),
                    title = "Units",
                    onClick = { /* Units implementation not required */ }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Data Management Section
                SectionHeader("Data Management")
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Share,
                    iconColor = Color(0xFF4CAF50),
                    title = "Export Data",
                    onClick = { showExportDialog = true }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // About Section
                SectionHeader("About")
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Lock,
                    iconColor = Color(0xFF4CAF50),
                    title = "Privacy Policy",
                    onClick = { /* Privacy policy implementation */ }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Info,
                    iconColor = Color(0xFF4CAF50),
                    title = "Terms of Service",
                    onClick = { /* Terms of service implementation */ }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Info,
                    iconColor = Color(0xFF4CAF50),
                    title = "Version",
                    trailing = appVersion,
                    showCopyIcon = true,
                    onClick = { }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Build,
                    iconColor = Color(0xFF4CAF50),
                    title = "Build Number",
                    trailing = buildNumber,
                    showCopyIcon = true,
                    onClick = { }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.Info,
                    iconColor = Color(0xFF4CAF50),
                    title = "Full Version",
                    trailing = "$appVersion ($buildNumber)",
                    showCopyIcon = true,
                    onClick = { }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Account Section
                SectionHeader("Account")
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileMenuItem(
                    icon = FeatherIconsCollection.ExitToApp,
                    iconColor = Color(0xFFFF3B30),
                    title = "Sign Out",
                    titleColor = Color(0xFFFF3B30),
                    onClick = { authViewModel.logout() }
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { 
                    Text(
                        "Export Data",
                        color = Color.White
                    ) 
                },
                text = {
                    Text(
                        "Export your health data to a CSV file?",
                        color = Color.White
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            exportLauncher.launch("trackit_data_$timestamp.csv")
                            showExportDialog = false
                        }
                    ) { 
                        Text("Export", color = Color(0xFF007AFF)) 
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel", color = Color(0xFF007AFF))
                    }
                },
                containerColor = Color(0xFF1C1C1E)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF8E8E93),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    titleColor: Color = Color.White,
    trailing: String? = null,
    showArrow: Boolean = true,
    showCopyIcon: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title
            Text(
                text = title,
                color = titleColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            // Trailing content
            if (trailing != null) {
                Text(
                    text = trailing,
                    color = Color(0xFF8E8E93),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Copy icon or arrow
            if (showCopyIcon) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Copy",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            } else if (showArrow) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


private fun exportMetricsToCsv(context: android.content.Context, uri: Uri, viewModel: HealthViewModel) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val writer = outputStream.bufferedWriter()
            
            // Write header
            writer.write("Metric,Value,Date\n")
            
            // Get all metrics
            val metrics = listOf(
                "Weight" to "kg",
                "Height" to "cm",
                "Body Fat" to "%",
                "Waist" to "cm",
                "Bicep" to "cm",
                "Chest" to "cm",
                "Thigh" to "cm",
                "Shoulder" to "cm"
            )
            
            // Write data for each metric
            metrics.forEach { (metricName, unit) ->
                viewModel.getMetricHistory(metricName, unit).forEach { entry ->
                    writer.write("$metricName,${entry.value},${entry.date}\n")
                }
            }
            
            writer.flush()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
