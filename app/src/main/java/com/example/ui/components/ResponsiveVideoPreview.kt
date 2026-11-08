package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

/**
 * Standard aspect ratio definitions for responsive video previewing.
 */
enum class VideoAspectRatio(
    val id: String,
    val label: String,
    val shortLabel: String,
    val ratio: Float,
    val widthRatio: Int,
    val heightRatio: Int,
    val platformHint: String,
    val standardResolution: String,
    val icon: ImageVector
) {
    SHORTS_9_16(
        id = "9:16",
        label = "9:16 Shorts",
        shortLabel = "9:16",
        ratio = 9f / 16f,
        widthRatio = 9,
        heightRatio = 16,
        platformHint = "Shorts • Reels • TikTok",
        standardResolution = "1080 × 1920",
        icon = Icons.Default.Smartphone
    ),
    LANDSCAPE_16_9(
        id = "16:9",
        label = "16:9 Landscape",
        shortLabel = "16:9",
        ratio = 16f / 9f,
        widthRatio = 16,
        heightRatio = 9,
        platformHint = "YouTube • Web • TV",
        standardResolution = "1920 × 1080",
        icon = Icons.Default.Tv
    ),
    SQUARE_1_1(
        id = "1:1",
        label = "1:1 Square",
        shortLabel = "1:1",
        ratio = 1f,
        widthRatio = 1,
        heightRatio = 1,
        platformHint = "Instagram Feed • Posts",
        standardResolution = "1080 × 1080",
        icon = Icons.Default.CropSquare
    ),
    PORTRAIT_4_5(
        id = "4:5",
        label = "4:5 Social",
        shortLabel = "4:5",
        ratio = 4f / 5f,
        widthRatio = 4,
        heightRatio = 5,
        platformHint = "IG & FB Feeds",
        standardResolution = "1080 × 1350",
        icon = Icons.Default.CropPortrait
    ),
    CINEMA_21_9(
        id = "21:9",
        label = "21:9 Cinema",
        shortLabel = "21:9",
        ratio = 21f / 9f,
        widthRatio = 21,
        heightRatio = 9,
        platformHint = "Ultrawide Cinema (2.39:1)",
        standardResolution = "2560 × 1080",
        icon = Icons.Default.Movie
    );

    companion object {
        fun fromRatio(ratio: Float): VideoAspectRatio {
            return entries.minByOrNull { kotlin.math.abs(it.ratio - ratio) } ?: LANDSCAPE_16_9
        }

        fun fromFormatString(format: String): VideoAspectRatio {
            return when {
                format.contains("9:16") -> SHORTS_9_16
                format.contains("1:1") -> SQUARE_1_1
                format.contains("4:5") -> PORTRAIT_4_5
                format.contains("21:9") -> CINEMA_21_9
                else -> LANDSCAPE_16_9
            }
        }
    }
}

/**
 * AspectRatioFrameLayout resize mode configuration for Media3 PlayerView.
 */
@OptIn(UnstableApi::class)
enum class VideoResizeMode(
    val label: String,
    val media3Mode: Int,
    val icon: ImageVector
) {
    FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT, Icons.Default.FitScreen),
    ZOOM("Fill/Crop", AspectRatioFrameLayout.RESIZE_MODE_ZOOM, Icons.Default.CropFree)
}

/**
 * High-performance, responsive video preview container using AndroidX Media3 [PlayerView].
 *
 * Supports:
 * - Fluid aspect ratio toggling (9:16 Shorts vs 16:9 Landscape, 1:1, 4:5, 21:9)
 * - Container-aware responsive bounds via BoxWithConstraints (never breaks screen height)
 * - Media3 PlayerView with configurable resize mode (Fit vs Zoom/Fill)
 * - Safe Zone guide overlays for Shorts, Reels, and TikTok
 * - ColorMatrix real-time shader filters
 * - Interactive custom playback controls (Seek, Play/Pause, Volume fade, Speed)
 * - Fast orientation flip (1-tap 16:9 ↔ 9:16 switch)
 */
@OptIn(UnstableApi::class)
@Composable
fun ResponsiveVideoPreview(
    modifier: Modifier = Modifier,
    videoUri: String? = null,
    title: String = "Video Preview",
    selectedAspectRatio: VideoAspectRatio = VideoAspectRatio.LANDSCAPE_16_9,
    onAspectRatioChange: (VideoAspectRatio) -> Unit = {},
    filterPreset: VideoFilterPreset = VideoFilterPreset.NONE,
    safeZoneType: SafeZoneType = SafeZoneType.NONE,
    onSafeZoneChange: (SafeZoneType) -> Unit = {},
    trimBounds: Pair<Long, Long>? = null,
    masterVolume: Float = 1.0f,
    fadeInSec: Float = 0f,
    fadeOutSec: Float = 0f,
    sceneMarkers: List<String> = emptyList(),
    visualPrompts: List<String> = emptyList(),
    sceneImages: List<String> = emptyList(),
    scriptText: String = "",
    onPositionChange: (Long) -> Unit = {},
    onMarkInPoint: ((Long) -> Unit)? = null,
    onMarkOutPoint: ((Long) -> Unit)? = null,
    onPickLocalVideo: ((String) -> Unit)? = null,
    onSceneSelect: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    // Local video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPickLocalVideo?.invoke(it.toString()) }
    }

    // Player & display state
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(60000L) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableStateOf(VideoResizeMode.FIT) }
    var isExpandedHeight by remember { mutableStateOf(false) }

    // Error & Simulation Fallback state
    var isSceneSimulationMode by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var playbackErrorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    // Smooth animated ratio for fluid aspect ratio morphing
    val animatedRatio by animateFloatAsState(
        targetValue = selectedAspectRatio.ratio,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "preview_aspect_ratio_anim"
    )

    // Standard high-reliability fallback sample streams
    val reliableStreams = remember {
        listOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        )
    }

    fun sanitizeMediaUri(raw: String?): String {
        if (raw.isNullOrBlank()) return reliableStreams.first()
        val lower = raw.trim().lowercase()
        if (raw.startsWith("content://") || raw.startsWith("file://")) {
            return raw.trim()
        }
        if (lower.contains("studio.youtube.com") || lower.contains("instagram.com/reel") ||
            lower.contains("facebook.com/watch") || lower.contains("youtube.com/watch") ||
            (!lower.endsWith(".mp4") && !lower.endsWith(".mkv") && !lower.endsWith(".webm") && !lower.contains("/sample/"))
        ) {
            return reliableStreams.first()
        }
        return raw.trim()
    }

    // Media3 ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_ALL
            volume = if (isMuted) 0f else masterVolume
        }
    }

    // Dynamic volume adjustment with fade envelope
    LaunchedEffect(masterVolume, isMuted, currentPositionMs, durationMs, fadeInSec, fadeOutSec) {
        if (isMuted) {
            exoPlayer.volume = 0f
            return@LaunchedEffect
        }
        val posSec = currentPositionMs / 1000f
        val durSec = (durationMs / 1000f).coerceAtLeast(1f)
        val remainingSec = (durSec - posSec).coerceAtLeast(0f)

        var factor = 1.0f
        if (fadeInSec > 0f && posSec < fadeInSec) {
            factor = (posSec / fadeInSec).coerceIn(0f, 1f)
        } else if (fadeOutSec > 0f && remainingSec < fadeOutSec) {
            factor = (remainingSec / fadeOutSec).coerceIn(0f, 1f)
        }
        exoPlayer.volume = (masterVolume * factor).coerceIn(0f, 1f)
    }

    // Set or update MediaItem when videoUri or reloadTrigger changes
    LaunchedEffect(videoUri, reloadTrigger) {
        val targetUri = sanitizeMediaUri(videoUri)
        try {
            hasPlaybackError = false
            playbackErrorMessage = null
            val mediaItem = MediaItem.fromUri(Uri.parse(targetUri))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        } catch (e: Exception) {
            hasPlaybackError = true
            isSceneSimulationMode = true
            playbackErrorMessage = e.message
        }
    }

    // Player listener for state changes and error recovery
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0) durationMs = d
                    hasPlaybackError = false
                    isBuffering = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                hasPlaybackError = true
                isBuffering = false
                isPlaying = false
                playbackErrorMessage = "Stream buffering error (${error.errorCodeName})"
                isSceneSimulationMode = true
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Pause on background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Playback ticker + Trimming Boundary Enforcement
    LaunchedEffect(isPlaying, isSceneSimulationMode, playbackSpeed, trimBounds) {
        while (isPlaying) {
            if (isSceneSimulationMode) {
                val stepMs = (100 * playbackSpeed).toLong()
                currentPositionMs = (currentPositionMs + stepMs)
                if (trimBounds != null) {
                    val (startMs, endMs) = trimBounds
                    if (currentPositionMs >= endMs || currentPositionMs < startMs) {
                        currentPositionMs = startMs
                    }
                } else if (currentPositionMs >= durationMs) {
                    currentPositionMs = 0L
                }
            } else {
                val pos = exoPlayer.currentPosition
                currentPositionMs = pos
                val d = exoPlayer.duration
                if (d > 0) durationMs = d

                if (trimBounds != null) {
                    val (startMs, endMs) = trimBounds
                    if (pos >= endMs || pos < startMs) {
                        exoPlayer.seekTo(startMs)
                        currentPositionMs = startMs
                    }
                }
            }
            onPositionChange(currentPositionMs)
            delay(100)
        }
    }

    val effectiveSceneCount = (if (sceneMarkers.isNotEmpty()) sceneMarkers.size else if (visualPrompts.isNotEmpty()) visualPrompts.size else 3).coerceAtLeast(1)
    val activeSceneIndex = if (durationMs > 0) {
        ((currentPositionMs.toFloat() / durationMs.toFloat()) * effectiveSceneCount).toInt().coerceIn(0, effectiveSceneCount - 1)
    } else 0

    fun togglePlayPause() {
        if (isSceneSimulationMode) {
            isPlaying = !isPlaying
        } else {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                isPlaying = false
            } else {
                if (trimBounds != null && (currentPositionMs < trimBounds.first || currentPositionMs >= trimBounds.second)) {
                    exoPlayer.seekTo(trimBounds.first)
                    currentPositionMs = trimBounds.first
                }
                exoPlayer.play()
                isPlaying = true
            }
        }
    }

    fun seekRelative(deltaMs: Long) {
        val minBound = trimBounds?.first ?: 0L
        val maxBound = trimBounds?.second ?: durationMs
        val newPos = (currentPositionMs + deltaMs).coerceIn(minBound, maxBound)
        currentPositionMs = newPos
        if (!isSceneSimulationMode) {
            exoPlayer.seekTo(newPos)
        }
        onPositionChange(newPos)
        showControls = true
    }

    fun seekTo(positionMs: Long) {
        val minBound = trimBounds?.first ?: 0L
        val maxBound = trimBounds?.second ?: durationMs
        val target = positionMs.coerceIn(minBound, maxBound)
        currentPositionMs = target
        if (!isSceneSimulationMode) {
            exoPlayer.seekTo(target)
        }
        onPositionChange(target)
    }

    fun toggleMute() {
        isMuted = !isMuted
        exoPlayer.volume = if (isMuted) 0f else masterVolume
    }

    fun cycleSpeed() {
        val nextSpeed = when (playbackSpeed) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 2.0f
            2.0f -> 0.75f
            else -> 1.0f
        }
        playbackSpeed = nextSpeed
        if (!isSceneSimulationMode) {
            exoPlayer.playbackParameters = PlaybackParameters(nextSpeed)
        }
    }

    fun flipOrientation() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val next = if (selectedAspectRatio == VideoAspectRatio.SHORTS_9_16) {
            VideoAspectRatio.LANDSCAPE_16_9
        } else {
            VideoAspectRatio.SHORTS_9_16
        }
        onAspectRatioChange(next)
    }

    fun retryStream() {
        isSceneSimulationMode = false
        hasPlaybackError = false
        playbackErrorMessage = null
        reloadTrigger++
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("responsive_video_preview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F141C)
        ),
        border = BorderStroke(1.dp, Color(0xFF222938))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar: Monitor Badge, Resolution Readout, and Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Monitor Tag & Resolution
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StudioEmerald)
                    )
                    Text(
                        text = "PLAYERVIEW PREVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = StudioPurple.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "${selectedAspectRatio.shortLabel} • ${selectedAspectRatio.standardResolution}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioPurple,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Quick Header Actions: Safe Zones, Fit/Fill, Flip, Expand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Safe Zone Cycle Button
                    Surface(
                        onClick = {
                            val next = when (safeZoneType) {
                                SafeZoneType.NONE -> SafeZoneType.SHORTS
                                SafeZoneType.SHORTS -> SafeZoneType.REELS
                                SafeZoneType.REELS -> SafeZoneType.TIKTOK
                                SafeZoneType.TIKTOK -> SafeZoneType.NONE
                            }
                            onSafeZoneChange(next)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (safeZoneType != SafeZoneType.NONE) StudioPurple.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("preview_safe_zone_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = "Safe Zones",
                                modifier = Modifier.size(12.dp),
                                tint = if (safeZoneType != SafeZoneType.NONE) StudioPurple else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (safeZoneType == SafeZoneType.NONE) "Guides" else safeZoneType.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (safeZoneType != SafeZoneType.NONE) StudioPurple else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Fit vs Fill/Crop Mode
                    Surface(
                        onClick = {
                            resizeMode = if (resizeMode == VideoResizeMode.FIT) VideoResizeMode.ZOOM else VideoResizeMode.FIT
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (resizeMode == VideoResizeMode.ZOOM) StudioAmber.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("preview_resize_mode_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = resizeMode.icon,
                                contentDescription = resizeMode.label,
                                modifier = Modifier.size(12.dp),
                                tint = if (resizeMode == VideoResizeMode.ZOOM) StudioAmber else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = resizeMode.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (resizeMode == VideoResizeMode.ZOOM) StudioAmber else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // 1-Tap Orientation Flip (9:16 ↔ 16:9)
                    FilledIconButton(
                        onClick = { flipOrientation() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = StudioCrimson.copy(alpha = 0.2f),
                            contentColor = StudioCrimson
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("preview_flip_orientation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Flip 9:16 vs 16:9",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Expand / Theater Height Toggle
                    FilledIconButton(
                        onClick = {
                            isExpandedHeight = !isExpandedHeight
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("preview_toggle_expanded_height")
                    ) {
                        Icon(
                            imageVector = if (isExpandedHeight) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isExpandedHeight) "Collapse view" else "Expand view",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Aspect Ratio Segmented Selector Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VideoAspectRatio.entries.forEach { option ->
                    val isSelected = selectedAspectRatio == option
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAspectRatioChange(option)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) StudioCrimson else Color(0xFF1A2130),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) StudioCrimson else Color(0xFF2C374A)
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("aspect_ratio_chip_${option.id.replace(':', '_')}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = option.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Responsive Viewport Stage (BoxWithConstraints for adaptive height and width containment)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF070A0F))
                    .border(BorderStroke(1.dp, Color(0xFF1E2638)), RoundedCornerShape(14.dp))
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                val availableWidth = maxWidth
                // Standard stage height bounded nicely for editing layout
                val stageMaxHeight = if (isExpandedHeight) 480.dp else 340.dp

                // Compute exact layout dimensions based on animated target aspect ratio
                // ratio = width / height => height = width / ratio, width = height * ratio
                val (previewWidth, previewHeight) = if (animatedRatio < 1.0f) {
                    // Portrait orientation (e.g. 9:16 Shorts or 4:5 Social)
                    // Keep height within the stage bounds and scale width accordingly
                    val h = stageMaxHeight
                    val w = (h.value * animatedRatio).dp.coerceAtMost(availableWidth)
                    Pair(w, h)
                } else {
                    // Landscape or square orientation (e.g. 16:9 or 21:9 or 1:1)
                    val w = availableWidth
                    val h = (w.value / animatedRatio).dp.coerceAtMost(stageMaxHeight)
                    Pair(w, h)
                }

                // Inner Viewport Stage Frame
                Box(
                    modifier = Modifier
                        .width(previewWidth)
                        .height(previewHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showControls = !showControls
                        }
                        .testTag("preview_player_viewport"),
                    contentAlignment = Alignment.Center
                ) {
                    // Video Content Layer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                if (filterPreset != VideoFilterPreset.NONE) {
                                    val (tintColor, blendMode) = when (filterPreset) {
                                        VideoFilterPreset.NONE -> Pair(Color.Transparent, BlendMode.SrcOver)
                                        VideoFilterPreset.GRAYSCALE -> Pair(Color(0xFF888888), BlendMode.Saturation)
                                        VideoFilterPreset.SEPIA -> Pair(Color(0xFF704214).copy(alpha = 0.55f), BlendMode.Color)
                                        VideoFilterPreset.HIGH_CONTRAST -> Pair(Color.Black.copy(alpha = 0.3f), BlendMode.Hardlight)
                                        VideoFilterPreset.CYBERPUNK -> Pair(Color(0xFFE000FF).copy(alpha = 0.35f), BlendMode.Color)
                                        VideoFilterPreset.WARM -> Pair(Color(0xFFFF9900).copy(alpha = 0.3f), BlendMode.Color)
                                        VideoFilterPreset.COOL -> Pair(Color(0xFF00B4D8).copy(alpha = 0.35f), BlendMode.Color)
                                    }
                                    drawRect(color = tintColor, blendMode = blendMode)
                                }
                            }
                    ) {
                        if (!isSceneSimulationMode) {
                            // AndroidView wrapping PlayerView (Media3 UI)
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        this.player = exoPlayer
                                        this.useController = false
                                        this.resizeMode = resizeMode.media3Mode
                                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                update = { playerView ->
                                    if (playerView.player != exoPlayer) {
                                        playerView.player = exoPlayer
                                    }
                                    if (playerView.resizeMode != resizeMode.media3Mode) {
                                        playerView.resizeMode = resizeMode.media3Mode
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("media3_player_view_instance")
                            )
                        } else {
                            // Storyboard Canvas Mode with real visual artwork
                            val activeImagePath = sceneImages.getOrNull(activeSceneIndex)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("responsive_visual_scene_container"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!activeImagePath.isNullOrBlank() && File(activeImagePath).exists()) {
                                    AsyncImage(
                                        model = File(activeImagePath),
                                        contentDescription = "Scene ${activeSceneIndex + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF380D3D))
                                            )
                                        )
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.2f),
                                            radius = w * 0.45f,
                                            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.4f)
                                        )
                                        drawRect(
                                            color = Color(0xFF0A0A14),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.65f),
                                            size = androidx.compose.ui.geometry.Size(w, h * 0.35f)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = StudioCrimson.copy(alpha = 0.9f)
                                        ) {
                                            Text(
                                                text = "SCENE ${activeSceneIndex + 1} OF $effectiveSceneCount",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Surface(
                                                onClick = {
                                                    if (activeSceneIndex > 0) {
                                                        val prevIdx = activeSceneIndex - 1
                                                        val targetPos = (prevIdx.toFloat() / effectiveSceneCount.toFloat() * durationMs).toLong()
                                                        seekTo(targetPos)
                                                        onSceneSelect(prevIdx)
                                                    }
                                                },
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color.Black.copy(alpha = 0.6f)
                                            ) {
                                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Scene", tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            Surface(
                                                onClick = {
                                                    if (activeSceneIndex < effectiveSceneCount - 1) {
                                                        val nextIdx = activeSceneIndex + 1
                                                        val targetPos = (nextIdx.toFloat() / effectiveSceneCount.toFloat() * durationMs).toLong()
                                                        seekTo(targetPos)
                                                        onSceneSelect(nextIdx)
                                                    }
                                                },
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color.Black.copy(alpha = 0.6f)
                                            ) {
                                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Scene", tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }

                                    val activePrompt = visualPrompts.getOrElse(activeSceneIndex) {
                                        "Cinematic AI Studio visual prompt rendering..."
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.75f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            if (scriptText.isNotBlank()) {
                                                Text(
                                                    text = "\"${scriptText.take(90)}\"",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StudioAmber,
                                                    maxLines = 2
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }
                                            Text(
                                                text = "🎨 $activePrompt",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.85f),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Safe Zone Guide Overlay
                    SafeZoneGuideOverlay(
                        safeZoneType = safeZoneType,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Buffering Indicator
                    if (isBuffering && !isSceneSimulationMode) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = StudioCrimson,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Playback error banner
                    if (hasPlaybackError && !isSceneSimulationMode) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Red.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = playbackErrorMessage ?: "Buffering error",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RETRY",
                                    color = Color.Yellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.clickable { retryStream() }
                                )
                            }
                        }
                    }

                    // Custom Overlay for PlayerView with play/pause, seek +/- 5s, mute/unmute, and editing controls
                    PlayerViewCustomOverlay(
                        isVisible = showControls,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        isMuted = isMuted,
                        playbackSpeed = playbackSpeed,
                        title = title,
                        trimBounds = trimBounds,
                        onPlayPauseToggle = { togglePlayPause() },
                        onSeekRelative = { deltaMs -> seekRelative(deltaMs) },
                        onSeekTo = { posMs -> seekTo(posMs) },
                        onMuteToggle = { toggleMute() },
                        onSpeedCycle = { cycleSpeed() },
                        onMarkInPoint = onMarkInPoint,
                        onMarkOutPoint = onMarkOutPoint,
                        onOpenLocalFile = { videoPickerLauncher.launch("video/*") },
                        sceneMarkers = sceneMarkers,
                        onSceneSelect = { idx -> onSceneSelect(idx) }
                    )
                }
            }
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
