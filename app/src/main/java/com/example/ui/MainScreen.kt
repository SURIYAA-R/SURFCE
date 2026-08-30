package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.JoinCollabDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.QueueSheet
import com.example.ui.components.WavyAtmosphereBackground
import com.example.ui.screens.collaborate.CollaborateScreen
import com.example.ui.screens.collaborate.CollaborativePlaylistDetailScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.AlbumDetailScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.playlists.PlaylistDetailScreen
import com.example.ui.screens.playlists.PlaylistsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.viewmodel.MusicPlayerViewModel

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Library : Screen("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    object Playlists : Screen("playlists", "Playlists", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic)
    object Collaborate : Screen("collaborate", "Live Rooms", Icons.Filled.Sensors, Icons.Outlined.Sensors)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Playlists,
    Screen.Collaborate,
    Screen.Profile
)

@Composable
fun MainScreen(
    viewModel: MusicPlayerViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playbackState by viewModel.playbackState.collectAsState()
    val isFullPlayerExpanded by viewModel.isFullPlayerExpanded.collectAsState()
    val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsState()
    val selectedSongForPlaylist by viewModel.selectedSongForPlaylist.collectAsState()
    val isCreatePlaylistDialogOpen by viewModel.isCreatePlaylistDialogOpen.collectAsState()
    val isJoinCollabDialogOpen by viewModel.isJoinCollabDialogOpen.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()

    val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

    WavyAtmosphereBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Mini Player (Visible when a song is loaded and full player is not expanded)
                    if (playbackState.currentSong != null && !isFullPlayerExpanded) {
                        MiniPlayer(
                            playbackState = playbackState,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onPlayNext = { viewModel.playNext() },
                            onExpandFullPlayer = { viewModel.setFullPlayerExpanded(true) }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .testTag("bottom_nav_bar")
                    ) {
                        bottomNavScreens.forEach { screen ->
                            val isSelected = currentRoute == screen.route && !isFullPlayerExpanded
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    // Shrink full player page if expanded
                                    if (isFullPlayerExpanded) {
                                        viewModel.setFullPlayerExpanded(false)
                                    }
                                    // Dismiss queue sheet if visible
                                    if (isQueueSheetVisible) {
                                        viewModel.setQueueSheetVisible(false)
                                    }
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue7692FF,
                                    selectedTextColor = PrimaryBlue7692FF,
                                    indicatorColor = PrimaryBlue7692FF.copy(alpha = 0.15f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToPlaylist = { playlistId ->
                                navController.navigate("playlist/$playlistId")
                            },
                            onNavigateToSearch = {
                                navController.navigate("search")
                            },
                            onNavigateToLibrary = {
                                navController.navigate(Screen.Library.route)
                            }
                        )
                    }

                    composable(Screen.Library.route) {
                        LibraryScreen(
                            viewModel = viewModel,
                            onNavigateToAlbum = { albumName ->
                                navController.navigate("album/$albumName")
                            },
                            onNavigateToArtist = { artistName ->
                                navController.navigate("search")
                            },
                            onNavigateToFolder = { folderName ->
                                navController.navigate("search")
                            }
                        )
                    }

                    composable(Screen.Playlists.route) {
                        PlaylistsScreen(
                            viewModel = viewModel,
                            onNavigateToPlaylist = { playlistId ->
                                navController.navigate("playlist/$playlistId")
                            }
                        )
                    }

                    composable(Screen.Collaborate.route) {
                        CollaborateScreen(
                            viewModel = viewModel,
                            onNavigateToCollabDetail = { playlistId ->
                                navController.navigate("collab/$playlistId")
                            }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            viewModel = viewModel
                        )
                    }

                    composable("search") {
                        SearchScreen(
                            viewModel = viewModel,
                            onNavigateToAlbum = { albumName ->
                                navController.navigate("album/$albumName")
                            },
                            onNavigateToArtist = { artistName -> }
                        )
                    }

                    composable(
                        route = "album/{albumName}",
                        arguments = listOf(navArgument("albumName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val albumName = backStackEntry.arguments?.getString("albumName") ?: ""
                        AlbumDetailScreen(
                            albumName = albumName,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                        PlaylistDetailScreen(
                            playlistId = playlistId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToCollab = { collabId ->
                                navController.navigate("collab/$collabId")
                            }
                        )
                    }

                    composable(
                        route = "collab/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                        CollaborativePlaylistDetailScreen(
                            playlistId = playlistId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                // Full Screen Player Sheet Overlay
                FullPlayerSheet(
                    isExpanded = isFullPlayerExpanded,
                    playbackState = playbackState,
                    onCollapse = { viewModel.setFullPlayerExpanded(false) },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onPlayNext = { viewModel.playNext() },
                    onPlayPrevious = { viewModel.playPrevious() },
                    onSeekTo = { posMs -> viewModel.seekTo(posMs) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onToggleRepeat = { viewModel.toggleRepeatMode() },
                    onToggleFavorite = {
                        val current = playbackState.currentSong
                        if (current != null) viewModel.toggleFavorite(current)
                    },
                    onOpenQueue = { viewModel.setQueueSheetVisible(true) },
                    onVolumeChange = { vol -> viewModel.setVolume(vol) }
                )

                // Playback Queue Sheet
                QueueSheet(
                    isOpen = isQueueSheetVisible,
                    playbackState = playbackState,
                    onDismiss = { viewModel.setQueueSheetVisible(false) },
                    onPlaySong = { song -> viewModel.playSong(song, playbackState.queue) },
                    onRemoveFromQueue = { index -> viewModel.removeFromQueue(index) },
                    onClearQueue = { viewModel.clearQueue() },
                    onMoveUp = { index -> viewModel.reorderQueue(index, index - 1) },
                    onMoveDown = { index -> viewModel.reorderQueue(index, index + 1) }
                )

                // Add to Playlist Dialog
                AddToPlaylistDialog(
                    song = selectedSongForPlaylist,
                    playlists = allPlaylists,
                    onDismiss = { viewModel.setSelectedSongForPlaylist(null) },
                    onSelectPlaylist = { playlistId, isCollab ->
                        val song = selectedSongForPlaylist
                        if (song != null) {
                            viewModel.addSongToPlaylist(playlistId, song.id, isCollab, song.title)
                        }
                    }
                )

                // Create Playlist Dialog
                CreatePlaylistDialog(
                    isOpen = isCreatePlaylistDialogOpen,
                    onDismiss = { viewModel.setCreatePlaylistDialogOpen(false) },
                    onCreate = { name, description, isCollab ->
                        viewModel.createPlaylist(name, description, isCollab)
                    }
                )

                // Join Collaborative Room Dialog
                JoinCollabDialog(
                    isOpen = isJoinCollabDialogOpen,
                    onDismiss = { viewModel.setJoinCollabDialogOpen(false) },
                    onJoin = { inviteCode ->
                        viewModel.joinCollabPlaylist(inviteCode)
                    }
                )
            }
        }
    }
}
