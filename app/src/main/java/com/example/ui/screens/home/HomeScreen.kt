package com.example.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OrganicWaveformVisualizer
import com.example.ui.components.PlaylistCard
import com.example.ui.components.SongListItem
import com.example.ui.components.SongThumbnail
import com.example.ui.components.SurfceLogo
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.viewmodel.MusicPlayerViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateToPlaylist: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedSongs.collectAsState()
    val mostPlayed by viewModel.mostPlayedSongs.collectAsState()
    val favorites by viewModel.favoriteSongs.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val collaborativePlaylists by viewModel.collaborativePlaylists.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val greeting = rememberGreeting()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Header with SurfceLogo & Quick Action Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        SurfceLogo(
                            fontSize = 24.sp,
                            showWaveBadge = true
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$greeting • ${allSongs.size} Local Tracks",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Theme Toggle (Light / Dark)
                        val currentMode = userSettings?.themeMode ?: "DARK"
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .testTag("home_theme_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (currentMode == "LIGHT") Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Theme",
                                tint = PrimaryBlue7692FF,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Scan Local Files Quick Action
                        IconButton(
                            onClick = { viewModel.scanDeviceMusic() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .testTag("home_scan_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan Local Files",
                                tint = PrimaryBlue7692FF,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Search Icon
                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .testTag("home_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PrimaryBlue7692FF,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Empty Local Music Notice if no local songs scanned yet
        if (allSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EmptyStateView(
                            title = "Local Music Library",
                            subtitle = "SURFCE plays music stored directly on your device. Tap below to scan and load your offline tracks.",
                            actionButtonText = if (isScanning) "Scanning Device..." else "Scan Local Audio Files",
                            onActionClick = { viewModel.scanDeviceMusic() },
                            icon = Icons.Default.Refresh
                        )
                    }
                }
            }
        }

        // 2. Continue Listening Hero Card
        item {
            val heroSong = playbackState.currentSong ?: recentlyPlayed.firstOrNull()
            if (heroSong != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = PrimaryBlue7692FF.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .clickable {
                            viewModel.playSong(heroSong, if (playbackState.queue.isNotEmpty()) playbackState.queue else recentlyPlayed)
                        }
                        .testTag("continue_listening_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue7692FF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CONTINUE LISTENING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryBlue7692FF.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SURFCE SYNC",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = PrimaryBlue7692FF
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SongThumbnail(
                                song = heroSong,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = PrimaryBlue7692FF.copy(alpha = 0.3f))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = heroSong.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${heroSong.artist} • ${heroSong.album}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (playbackState.currentSong?.id == heroSong.id) {
                                        viewModel.togglePlayPause()
                                    } else {
                                        viewModel.playSong(heroSong, recentlyPlayed)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(12.dp, CircleShape, spotColor = PrimaryBlue7692FF)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue7692FF)
                                    .testTag("hero_play_btn")
                            ) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying && playbackState.currentSong?.id == heroSong.id) Icons.Default.Favorite else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        OrganicWaveformVisualizer(
                            isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == heroSong.id,
                            progress = if (playbackState.currentSong?.id == heroSong.id) playbackState.progress else 0.4f,
                            amplitudes = playbackState.waveformAmplitudes,
                            height = 36.dp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }

        // 3. Recently Played Carousel
        item {
            if (recentlyPlayed.isNotEmpty()) {
                SectionHeader(
                    title = "Recently Played",
                    actionText = "See All",
                    onActionClick = onNavigateToLibrary
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentlyPlayed, key = { it.id }) { song ->
                        RecentSongCard(
                            song = song,
                            isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                            onClick = { viewModel.playSong(song, recentlyPlayed) }
                        )
                    }
                }
            }
        }

        // 4. Collaborative Playlists Spotlight
        item {
            if (collaborativePlaylists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Collaborative Rooms",
                    subtitle = "Listen & curate together with friends"
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(collaborativePlaylists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            isLarge = true,
                            onClick = { onNavigateToPlaylist(playlist.id) },
                            onPlayAll = {
                                viewModel.getSongsForPlaylist(playlist.id)
                                onNavigateToPlaylist(playlist.id)
                            }
                        )
                    }
                }
            }
        }

        // 5. Your Playlists
        item {
            if (playlists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Your Playlists",
                    actionText = "New Playlist",
                    onActionClick = { viewModel.setCreatePlaylistDialogOpen(true) }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onNavigateToPlaylist(playlist.id) },
                            onPlayAll = { onNavigateToPlaylist(playlist.id) }
                        )
                    }
                }
            }
        }

        // 6. Most Played / Top Tracks
        item {
            if (mostPlayed.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(
                    title = "Most Played",
                    subtitle = "Your top audio tracks"
                )
            }
        }

        items(mostPlayed.take(5), key = { it.id }) { song ->
            SongListItem(
                song = song,
                isPlaying = playbackState.isPlaying,
                isCurrentSong = playbackState.currentSong?.id == song.id,
                onClick = { viewModel.playSong(song, mostPlayed) },
                onFavoriteToggle = { viewModel.toggleFavorite(song) },
                onAddToQueue = { viewModel.addToQueue(song) },
                onAddToPlaylist = { viewModel.setSelectedSongForPlaylist(song) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // 7. Favorites Quick Mix
        if (favorites.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(
                    title = "Favorites",
                    subtitle = "${favorites.size} songs you love",
                    actionText = "Play All",
                    onActionClick = {
                        if (favorites.isNotEmpty()) viewModel.playSong(favorites.first(), favorites)
                    }
                )
            }

            items(favorites.take(4), key = { "fav_${it.id}" }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playbackState.isPlaying,
                    isCurrentSong = playbackState.currentSong?.id == song.id,
                    onClick = { viewModel.playSong(song, favorites) },
                    onFavoriteToggle = { viewModel.toggleFavorite(song) },
                    onAddToQueue = { viewModel.addToQueue(song) },
                    onAddToPlaylist = { viewModel.setSelectedSongForPlaylist(song) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = PrimaryBlue7692FF,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun RecentSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(136.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("recent_song_card_${song.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                SongThumbnail(
                    song = song,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isPlaying) PrimaryBlue7692FF else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Late night vibes"
    }
}
