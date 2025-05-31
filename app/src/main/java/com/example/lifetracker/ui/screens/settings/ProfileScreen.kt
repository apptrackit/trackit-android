package com.example.lifetracker.ui.screens.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.data.repository.MetricsRepository
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: HealthViewModel,
) {
    val context = LocalContext.current

    var userName by rememberSaveable { mutableStateOf(viewModel.getUserName() ?: "User") }
    var gender by rememberSaveable { mutableStateOf(viewModel.getGender() ?: "Not set") }
    var birthYear by rememberSaveable { mutableStateOf(viewModel.getBirthYear() ?: 2000) }

    var showNameDialog by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showBirthYearDialog by remember { mutableStateOf(false) }

    val age = remember(birthYear) {
        Calendar.getInstance().get(Calendar.YEAR) - birthYear
    }

    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 20.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton( // Changed back to IconButton for proper animation handling
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Profile",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile avatar
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // User name - Clickable to edit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showNameDialog = true }
            ) {
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 8.dp)
                )
            }


            Spacer(modifier = Modifier.height(28.dp))

            // Info cards
            ProfileInfoCard(
                label = "Gender",
                value = gender,
                onClick = { showGenderDialog = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileInfoCard(
                label = "Age",
                value = age.toString(),
                onClick = { showBirthYearDialog = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileInfoCard(label = "App Version", value = appVersion.toString())

            Spacer(modifier = Modifier.height(32.dp))

            // Creative: motivational quote
            Text(
                text = "\"Every day is progress.\"",
                color = Color(0xFFAAAAAA),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Name Dialog
        if (showNameDialog) {
            var nameInput by rememberSaveable { mutableStateOf(userName) }
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Set Name") },
                text = {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                userName = nameInput
                                viewModel.setUserName(nameInput)
                                showNameDialog = false
                            }
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Gender Dialog
        if (showGenderDialog) {
            AlertDialog(
                onDismissRequest = { showGenderDialog = false },
                title = { Text("Select Gender") },
                text = {
                    Column {
                        val genderOptions = listOf("Male", "Female", "Not set")
                        genderOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gender = option
                                        viewModel.setGender(option)
                                        showGenderDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = gender == option,
                                    onClick = {
                                        gender = option
                                        viewModel.setGender(option)
                                        showGenderDialog = false
                                    }
                                )
                                Text(option, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        // Birth Year Dialog
        if (showBirthYearDialog) {
            var birthYearInput by rememberSaveable { mutableStateOf(birthYear.toString()) }
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            AlertDialog(
                onDismissRequest = { showBirthYearDialog = false },
                title = { Text("Set Birth Year") },
                text = {
                    OutlinedTextField(
                        value = birthYearInput,
                        onValueChange = { birthYearInput = it.filter { char -> char.isDigit() }.take(4) },
                        label = { Text("Birth Year (YYYY)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val year = birthYearInput.toIntOrNull()
                            if (year != null && year in 1900..currentYear) {
                                birthYear = year
                                viewModel.setBirthYear(year)
                                showBirthYearDialog = false
                            }
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showBirthYearDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(label: String, value: String, onClick: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = Color(0xFF181818),
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color(0xFF4CAF50),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    tint = Color(0xFF4CAF50).copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
