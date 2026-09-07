package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Song
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.ui.theme.SoftBlueSecondary

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayNext: (() -> Unit)? = null,
    showTrackNumber: Boolean = false
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isCurrentSong) PrimaryBlue7692FF.copy(alpha = 0.12f) else Color.Transparent
            )
            .then(
                if (isCurrentSong) Modifier.border(1.dp, PrimaryBlue7692FF.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("song_list_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showTrackNumber) {
            val trackNum = if (song.trackNumber > 1000) song.trackNumber % 1000 else song.trackNumber
            Text(
                text = if (trackNum > 0) "$trackNum" else "-",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(26.dp)
            )
        }

        // Album Artwork thumbnail
        SongThumbnail(
            song = song,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = if (isCurrentSong) PrimaryBlue7692FF else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Playing indicator or duration
        if (isCurrentSong) {
            MiniWaveformIndicator(
                isPlaying = isPlaying,
                modifier = Modifier.padding(end = 4.dp),
                color = PrimaryBlue7692FF
            )
        } else {
            Text(
                text = song.durationFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Favorite Button
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier
                .size(40.dp)
                .testTag("song_fav_btn_${song.id}")
        ) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (song.isFavorite) "Favorited" else "Favorite",
                tint = if (song.isFavorite) FavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // More options dropdown
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("song_more_btn_${song.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Play Now", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        menuExpanded = false
                        onClick()
                    }
                )
                if (onPlayNext != null) {
                    DropdownMenuItem(
                        text = { Text("Play Next", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            menuExpanded = false
                            onPlayNext()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Add to Queue", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        menuExpanded = false
                        onAddToQueue()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        menuExpanded = false
                        onAddToPlaylist()
                    }
                )
            }
        }
    }
}

@Composable
fun SongThumbnail(
    song: Song,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val artUri = song.albumArtUri
        val drawableResId = when {
            artUri?.contains("midnight_waves") == true -> R.drawable.img_cover_midnight_waves
            artUri?.contains("neon_echoes") == true -> R.drawable.img_cover_neon_echoes
            artUri?.contains("deep_focus") == true -> R.drawable.img_cover_deep_focus
            song.title.contains("Midnight") || song.album.contains("Oceanic") -> R.drawable.img_cover_midnight_waves
            song.title.contains("Neon") || song.album.contains("Velvet") -> R.drawable.img_cover_neon_echoes
            song.title.contains("Focus") || song.album.contains("Resonant") -> R.drawable.img_cover_deep_focus
            else -> null
        }

        if (!artUri.isNullOrBlank() && !artUri.startsWith("drawable://")) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artUri)
                    .crossfade(true)
                    .build(),
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (drawableResId != null) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Stylized artistic gradient thumbnail
            val hash = (song.album + song.artist).hashCode()
            val gradientColors = when (Math.abs(hash) % 4) {
                0 -> listOf(PrimaryBlue7692FF, Color(0xFF2B7878))
                1 -> listOf(Color(0xFF2B7878), Color(0xFF0F3838))
                2 -> listOf(PrimaryBlue7692FF, Color(0xFF1E3A5F))
                else -> listOf(Color(0xFF388E8E), PrimaryBlue7692FF)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
