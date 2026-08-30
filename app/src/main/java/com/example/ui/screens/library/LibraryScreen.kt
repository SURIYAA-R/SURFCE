package com.example.ui.screens.library

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.AlbumCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SongListItem
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.ui.theme.SoftBlueSecondary
import com.example.viewmodel.MusicPlayerViewModel

enum class LibraryTab(val title: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    GENRES("Genres"),
    FOLDERS("Folders"),
    FAVORITES("Favorites"),
    RECENTLY_ADDED("Recently Added")
}

enum class SongSortOrder(val title: String) {
    TITLE_ASC("Title (A-Z)"),
    ARTIST_ASC("Artist (A-Z)"),
    DATE_ADDED_DESC("Recently Added"),
    DURATION_DESC("Longest First")
}

@Composable
fun LibraryScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val recentlyAdded by viewModel.recentlyAddedSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanMessage by viewModel.scanMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var sortOrder by remember { mutableStateOf(SongSortOrder.TITLE_ASC) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Header & Scan action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${allSongs.size} tracks indexed offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Scan Device button
            Button(
                onClick = { viewModel.scanDeviceMusic() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue7692FF.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
                enabled = !isScanning,
                modifier = Modifier.testTag("scan_device_music_btn")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryBlue7692FF
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scanning...", color = PrimaryBlue7692FF, style = MaterialTheme.typography.labelSmall)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan",
                        tint = PrimaryBlue7692FF,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Device", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Scan Toast banner
        if (scanMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlue7692FF.copy(alpha = 0.15f))
                    .clickable { viewModel.clearScanMessage() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scanMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue7692FF
                )
            }
        }

        // Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = PrimaryBlue7692FF,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = PrimaryBlue7692FF,
                    height = 3.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        ) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selectedTab == tab) PrimaryBlue7692FF else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Content per selected tab
        when (selectedTab) {
            LibraryTab.SONGS -> {
                val sortedSongs = remember(allSongs, sortOrder) {
                    when (sortOrder) {
                        SongSortOrder.TITLE_ASC -> allSongs.sortedBy { it.title.lowercase() }
                        SongSortOrder.ARTIST_ASC -> allSongs.sortedBy { it.artist.lowercase() }
                        SongSortOrder.DATE_ADDED_DESC -> allSongs.sortedByDescending { it.dateAddedTimestamp }
                        SongSortOrder.DURATION_DESC -> allSongs.sortedByDescending { it.durationMs }
                    }
                }

                // Controls row: Play All, Shuffle, Sort By
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(
                            onClick = {
                                if (sortedSongs.isNotEmpty()) viewModel.playSong(sortedSongs.first(), sortedSongs)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue7692FF)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play All", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (sortedSongs.isNotEmpty()) {
                                    val shuffled = sortedSongs.shuffled()
                                    viewModel.playSong(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = SoftBlueSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Sort dropdown
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { sortMenuOpen = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = sortOrder.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            SongSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.title, color = if (order == sortOrder) PrimaryBlue7692FF else MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        sortOrder = order
                                        sortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (sortedSongs.isEmpty()) {
                    EmptyStateView(
                        title = "No Music Found",
                        subtitle = "Scan your device to import songs or use built-in ambient tracks.",
                        actionButtonText = "Scan Device",
                        onActionClick = { viewModel.scanDeviceMusic() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 100.dp)
                    ) {
                        items(sortedSongs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = playbackState.isPlaying,
                                isCurrentSong = playbackState.currentSong?.id == song.id,
                                onClick = { viewModel.playSong(song, sortedSongs) },
                                onFavoriteToggle = { viewModel.toggleFavorite(song) },
                                onAddToQueue = { viewModel.addToQueue(song) },
                                onAddToPlaylist = { viewModel.setSelectedSongForPlaylist(song) }
                            )
                        }
                    }
                }
            }

            LibraryTab.ALBUMS -> {
                if (albums.isEmpty()) {
                    EmptyStateView(
                        title = "No Albums Found",
                        subtitle = "Scan your device to populate your album catalog."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(albums, key = { it }) { album ->
                            val albumSongs = allSongs.filter { it.album == album }
                            val artist = albumSongs.firstOrNull()?.artist ?: "Various Artists"
                            AlbumCard(
                                albumName = album,
                                artistName = artist,
                                onClick = { onNavigateToAlbum(album) }
                            )
                        }
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                if (artists.isEmpty()) {
                    EmptyStateView(
                        title = "No Artists Found",
                        subtitle = "Scan your device to group tracks by artists."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(artists, key = { it }) { artist ->
                            val trackCount = allSongs.count { it.artist == artist }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToArtist(artist) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue7692FF.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = PrimaryBlue7692FF,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = artist,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$trackCount tracks",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.GENRES -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(genres, key = { it }) { genre ->
                        val count = allSongs.count { it.genre == genre }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable {
                                    val genreTracks = allSongs.filter { it.genre == genre }
                                    if (genreTracks.isNotEmpty()) viewModel.playSong(genreTracks.first(), genreTracks)
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = PrimaryBlue7692FF,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$count tracks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Genre",
                                tint = PrimaryBlue7692FF
                            )
                        }
                    }
                }
            }

            LibraryTab.FOLDERS -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(folders, key = { it }) { folder ->
                        val count = allSongs.count { it.folderName == folder }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { onNavigateToFolder(folder) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = PrimaryBlue7692FF,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = folder,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$count tracks in directory",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            LibraryTab.FAVORITES -> {
                if (favoriteSongs.isEmpty()) {
                    EmptyStateView(
                        title = "No Favorites Yet",
                        subtitle = "Tap the heart icon on any song to add it to your favorites."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 100.dp)
                    ) {
                        items(favoriteSongs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = playbackState.isPlaying,
                                isCurrentSong = playbackState.currentSong?.id == song.id,
                                onClick = { viewModel.playSong(song, favoriteSongs) },
                                onFavoriteToggle = { viewModel.toggleFavorite(song) },
                                onAddToQueue = { viewModel.addToQueue(song) },
                                onAddToPlaylist = { viewModel.setSelectedSongForPlaylist(song) }
                            )
                        }
                    }
                }
            }

            LibraryTab.RECENTLY_ADDED -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 100.dp)
                ) {
                    items(recentlyAdded, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = playbackState.isPlaying,
                            isCurrentSong = playbackState.currentSong?.id == song.id,
                            onClick = { viewModel.playSong(song, recentlyAdded) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song) },
                            onAddToQueue = { viewModel.addToQueue(song) },
                            onAddToPlaylist = { viewModel.setSelectedSongForPlaylist(song) }
                        )
                    }
                }
            }
        }
    }
}
