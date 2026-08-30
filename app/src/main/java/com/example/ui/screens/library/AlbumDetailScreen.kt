package com.example.ui.screens.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.components.SongListItem
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.viewmodel.MusicPlayerViewModel

@Composable
fun AlbumDetailScreen(
    albumName: String,
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs.collectAsState()
    val albumSongs = allSongs.filter { it.album == albumName }
    val artist = albumSongs.firstOrNull()?.artist ?: "Unknown Artist"
    val firstArtUri = albumSongs.firstOrNull { !it.albumArtUri.isNullOrBlank() }?.albumArtUri
    val playbackState by viewModel.playbackState.collectAsState()

    val totalDurationMs = albumSongs.sumOf { it.durationMs }
    val totalMins = totalDurationMs / 60000

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("album_detail_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            // Top Navigation & Album Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val coverRes = when {
                    albumName.contains("Velvet") -> R.drawable.img_cover_neon_echoes
                    albumName.contains("Resonant") -> R.drawable.img_cover_deep_focus
                    albumName.contains("Midnight") || albumName.contains("Oceanic") -> R.drawable.img_cover_midnight_waves
                    else -> null
                }

                if (!firstArtUri.isNullOrBlank() && !firstArtUri.startsWith("drawable://")) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(firstArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = albumName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (coverRes != null) {
                    Image(
                        painter = painterResource(id = coverRes),
                        contentDescription = albumName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Stylized procedural gradient cover
                    val hash = albumName.hashCode()
                    val gradientColors = when (Math.abs(hash) % 4) {
                        0 -> listOf(PrimaryBlue7692FF, Color(0xFF1E3A5F))
                        1 -> listOf(Color(0xFF2B7878), Color(0xFF0F3838))
                        2 -> listOf(PrimaryBlue7692FF, Color(0xFF2B7878))
                        else -> listOf(Color(0xFF388E8E), PrimaryBlue7692FF)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                // Bottom Overlay Gradient for clean text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x77000000),
                                    if (isDark) Color(0x77080C14) else Color(0x77F4F6FC),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .testTag("album_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Title info at bottom of hero
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$artist • ${albumSongs.size} tracks • $totalMins min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action Buttons Row (Play All, Shuffle, Add Album to Queue)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (albumSongs.isNotEmpty()) viewModel.playSong(albumSongs.first(), albumSongs)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue7692FF
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play All", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (albumSongs.isNotEmpty()) {
                            val shuffled = albumSongs.shuffled()
                            viewModel.playSong(shuffled.first(), shuffled)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = PrimaryBlue7692FF)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        if (albumSongs.isNotEmpty()) {
                            viewModel.addAlbumToQueue(albumSongs)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue7692FF.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.QueueMusic, contentDescription = "Add album to queue", tint = PrimaryBlue7692FF)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Queue", color = PrimaryBlue7692FF, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        // Tracklist
        items(albumSongs, key = { it.id }) { song ->
            SongListItem(
                song = song,
                isPlaying = playbackState.isPlaying,
                isCurrentSong = playbackState.currentSong?.id == song.id,
                onClick = { viewModel.playSong(song, albumSongs) },
                onFavoriteToggle = { viewModel.toggleFavorite(song) },
                onAddToQueue = { viewModel.addToQueue(song) },
                onPlayNext = { viewModel.playNextInQueue(song) },
                onAddToPlaylist = { viewModel.setSelectedSongForPlaylist(song) },
                showTrackNumber = true,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
