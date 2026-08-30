package com.example.ui.screens.playlists

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PlaylistCard
import com.example.ui.components.SongListItem
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.viewmodel.MusicPlayerViewModel

enum class PlaylistFilter(val label: String) {
    ALL("All"),
    PERSONAL("Personal"),
    COLLABORATIVE("Collaborative"),
    FAVORITES("Favorites")
}

@Composable
fun PlaylistsScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateToPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val personalPlaylists by viewModel.personalPlaylists.collectAsState()
    val collaborativePlaylists by viewModel.collaborativePlaylists.collectAsState()

    var activeFilter by remember { mutableStateOf(PlaylistFilter.ALL) }

    val displayedPlaylists = remember(activeFilter, allPlaylists, personalPlaylists, collaborativePlaylists) {
        when (activeFilter) {
            PlaylistFilter.ALL -> allPlaylists
            PlaylistFilter.PERSONAL -> personalPlaylists
            PlaylistFilter.COLLABORATIVE -> collaborativePlaylists
            PlaylistFilter.FAVORITES -> allPlaylists.filter { it.isFavorite }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlists_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${displayedPlaylists.size} collections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.setCreatePlaylistDialogOpen(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue7692FF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_playlist_top_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlaylistFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue7692FF,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = activeFilter == filter,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = PrimaryBlue7692FF
                        )
                    )
                }
            }

            // Playlists Grid
            if (displayedPlaylists.isEmpty()) {
                EmptyStateView(
                    title = "Your playlists are waiting.",
                    subtitle = "Create your first mix or invite friends to a collaborative session.",
                    actionButtonText = "Create Playlist",
                    onActionClick = { viewModel.setCreatePlaylistDialogOpen(true) },
                    icon = Icons.Default.QueueMusic
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 155.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedPlaylists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onNavigateToPlaylist(playlist.id) },
                            onPlayAll = { onNavigateToPlaylist(playlist.id) },
                            onDelete = { viewModel.deletePlaylist(playlist.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    onNavigateToCollab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val playlist by viewModel.getPlaylistById(playlistId).collectAsState(initial = null)
    val playlistSongs by viewModel.getSongsForPlaylist(playlistId).collectAsState(initial = emptyList())
    val playbackState by viewModel.playbackState.collectAsState()

    val currentPlaylist = playlist ?: return

    val totalDurationMs = playlistSongs.sumOf { it.durationMs }
    val totalMins = totalDurationMs / 60000

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                val coverRes = when {
                    currentPlaylist.coverUri?.contains("neon") == true -> R.drawable.img_cover_neon_echoes
                    currentPlaylist.coverUri?.contains("deep") == true -> R.drawable.img_cover_deep_focus
                    else -> R.drawable.img_cover_midnight_waves
                }

                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = currentPlaylist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x88000000),
                                    if (isDark) Color(0x66080C14) else Color(0x66F4F6FC),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Top navigation row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.toggleFavoritePlaylist(currentPlaylist.id, currentPlaylist.isFavorite) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = if (currentPlaylist.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Fav",
                                tint = if (currentPlaylist.isFavorite) FavoriteRed else Color.White
                            )
                        }
                    }
                }

                // Info Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    if (currentPlaylist.isCollaborative) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryBlue7692FF.copy(alpha = 0.2f))
                                .clickable { onNavigateToCollab(currentPlaylist.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = PrimaryBlue7692FF, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COLLABORATIVE ROOM • Tap to view live room", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue7692FF)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = currentPlaylist.name,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (currentPlaylist.description.isNotBlank()) {
                        Text(
                            text = currentPlaylist.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Curated by ${currentPlaylist.ownerName} • ${playlistSongs.size} songs • $totalMins min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (playlistSongs.isNotEmpty()) viewModel.playSong(playlistSongs.first(), playlistSongs)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue7692FF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play All", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (playlistSongs.isNotEmpty()) {
                            val shuffled = playlistSongs.shuffled()
                            viewModel.playSong(shuffled.first(), shuffled)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = PrimaryBlue7692FF)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Song items in playlist
        if (playlistSongs.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Playlist is empty",
                    subtitle = "Add songs from your library by tapping the 3 dots menu on any song."
                )
            }
        } else {
            items(playlistSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playbackState.isPlaying,
                    isCurrentSong = playbackState.currentSong?.id == song.id,
                    onClick = { viewModel.playSong(song, playlistSongs) },
                    onFavoriteToggle = { viewModel.toggleFavorite(song) },
                    onAddToQueue = { viewModel.addToQueue(song) },
                    onPlayNext = { viewModel.playNextInQueue(song) },
                    onAddToPlaylist = { viewModel.setSelectedSongForPlaylist(song) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
