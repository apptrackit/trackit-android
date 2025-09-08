package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.data.model.SyncState
import com.ballabotond.trackit.ui.theme.FeatherIcon
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = when {
                                !syncState.isOnline -> Color.Red
                                syncState.isSyncing -> Color.Yellow
                                syncState.failedUploads > 0 -> Color(0xFFFF9800)
                                syncState.pendingUploads > 0 -> Color.Blue
                                else -> Color.Green
                            },
                            shape = CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = when {
                            !syncState.isOnline -> "Offline"
                            syncState.isSyncing -> "Syncing..."
                            syncState.failedUploads > 0 -> "Sync issues"
                            syncState.pendingUploads > 0 -> "Pending sync"
                            else -> "Synced"
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (syncState.lastSyncTimestamp > 0) {
                        val lastSyncTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(syncState.lastSyncTimestamp))
                        Text(
                            text = "Last sync: $lastSyncTime",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Action button
            if (syncState.failedUploads > 0 || (!syncState.isOnline && syncState.pendingUploads > 0)) {
                TextButton(
                    onClick = onRetrySync,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4CAF50))
                ) {
                    FeatherIcon(
                        icon = FeatherIconsCollection.RefreshCw,
                        tint = Color(0xFF4CAF50),
                        size = 16.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry", fontSize = 12.sp)
                }
            }
            
            // Pending/Failed counts
            if (syncState.pendingUploads > 0 || syncState.failedUploads > 0) {
                Row {
                    if (syncState.pendingUploads > 0) {
                        Text(
                            text = "${syncState.pendingUploads}",
                            color = Color.Blue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (syncState.failedUploads > 0) {
                        if (syncState.pendingUploads > 0) {
                            Text(
                                text = "/",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "${syncState.failedUploads}",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
