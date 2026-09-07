package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LightTealAccent
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.ui.theme.SoftBlueSecondary
import com.example.ui.theme.SyncPendingOrange
import com.example.viewmodel.MusicPlayerViewModel

@Composable
fun ProfileScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val collabPlaylists by viewModel.collaborativePlaylists.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()

    var eqMenuOpen by remember { mutableStateOf(false) }
    var sleepTimerMenuOpen by remember { mutableStateOf(false) }

    val currentThemeMode = userSettings?.themeMode ?: "DARK"

    val eqPresets = listOf("Balanced Flow", "Bass Wave", "Electronic Synth", "Vocal Clarity", "Chill Ambient")
    val sleepOptions = listOf(0 to "Off", 15 to "15 Minutes", 30 to "30 Minutes", 45 to "45 Minutes", 60 to "60 Minutes")

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Fresh Profile & Space Header Card (No personal names)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, PrimaryBlue7692FF.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(PrimaryBlue7692FF, LightTealAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Audio Space",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Local Audio Hub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "SURFCE Personal Soundspace",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlue7692FF
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(count = allSongs.size.toString(), label = "Tracks")
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        StatItem(count = allPlaylists.size.toString(), label = "Playlists")
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        StatItem(count = collabPlaylists.size.toString(), label = "Live Rooms")
                    }
                }
            }
        }

        // Section: Appearance & Theme (Dark & Light Mode Switcher)
        item {
            SectionTitle("Appearance & Theme")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme",
                            tint = PrimaryBlue7692FF,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "App Theme Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Primary color: #7692FF. Switch between Dark and Light mode.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dark Option
                        FilterChip(
                            selected = currentThemeMode == "DARK",
                            onClick = { viewModel.updateThemeMode("DARK") },
                            label = { Text("Dark") },
                            leadingIcon = {
                                Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue7692FF,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Light Option
                        FilterChip(
                            selected = currentThemeMode == "LIGHT",
                            onClick = { viewModel.updateThemeMode("LIGHT") },
                            label = { Text("Light") },
                            leadingIcon = {
                                Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue7692FF,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // System Option
                        FilterChip(
                            selected = currentThemeMode == "SYSTEM",
                            onClick = { viewModel.updateThemeMode("SYSTEM") },
                            label = { Text("System") },
                            leadingIcon = {
                                Icon(Icons.Default.BrightnessAuto, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue7692FF,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Sync & Cloud Architecture
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("Offline Architecture & Cloud Sync")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Online Synchronization",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sync collaborative playlist edits and room presence when connected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userSettings?.isOnlineSyncEnabled ?: true,
                            onCheckedChange = { viewModel.updateOnlineSyncMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue7692FF
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (pendingSyncCount > 0) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (pendingSyncCount > 0) SyncPendingOrange else OnlineGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (pendingSyncCount > 0) "$pendingSyncCount pending sync updates" else "All metadata in sync",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.syncNow()
                                Toast.makeText(context, "Sync complete", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue7692FF.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sync Now", color = PrimaryBlue7692FF, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Section: Audio Settings & Engine
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("Audio Engine & Tuning")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Equalizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { eqMenuOpen = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = PrimaryBlue7692FF, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Equalizer Preset", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(userSettings?.equalizerPreset ?: "Balanced Flow", style = MaterialTheme.typography.bodySmall, color = SoftBlueSecondary)
                            }
                        }

                        Box {
                            Text("Change", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue7692FF)
                            DropdownMenu(
                                expanded = eqMenuOpen,
                                onDismissRequest = { eqMenuOpen = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                eqPresets.forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset, color = if (userSettings?.equalizerPreset == preset) PrimaryBlue7692FF else MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            viewModel.updateEqualizer(preset)
                                            eqMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sleep Timer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sleepTimerMenuOpen = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = PrimaryBlue7692FF, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Sleep Timer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                val currentMin = userSettings?.sleepTimerMinutes ?: 0
                                Text(if (currentMin > 0) "$currentMin minutes" else "Disabled", style = MaterialTheme.typography.bodySmall, color = SoftBlueSecondary)
                            }
                        }

                        Box {
                            Text("Set", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue7692FF)
                            DropdownMenu(
                                expanded = sleepTimerMenuOpen,
                                onDismissRequest = { sleepTimerMenuOpen = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                sleepOptions.forEach { (mins, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = if (userSettings?.sleepTimerMinutes == mins) PrimaryBlue7692FF else MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            viewModel.updateSleepTimer(mins)
                                            sleepTimerMenuOpen = false
                                            Toast.makeText(context, if (mins > 0) "Sleep timer set for $mins min" else "Sleep timer turned off", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Storage & Media Library
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("Media Library Management")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.scanDeviceMusic() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = PrimaryBlue7692FF, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Rescan Local Storage", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Indexed ${allSongs.size} audio files on device", style = MaterialTheme.typography.bodySmall, color = SoftBlueSecondary)
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.scanDeviceMusic()
                                Toast.makeText(context, "Scanning local device storage...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue7692FF),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Scan", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Section: About SURFCE
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("About")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SURFCE Audio Player v1.0",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A sleek offline-first personal audio player powered entirely by your local files with organic waveform visualizer scrubbing, modern #31363F slate styling with glacier accents, and seamless Light & Dark themes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        ),
        color = PrimaryBlue7692FF,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = PrimaryBlue7692FF
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
