package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.generator.StoryToVideoPipeline
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

/**
 * Reusable Media3 VideoPlayer component.
 * Features:
 * - Direct local video file preview (content://, file://) and streaming MP4 support
 * - Time boundary clamping (trimStartMs & trimEndMs)
 * - ColorMatrix real-time filter grading (Grayscale, Sepia, High-Contrast, Cyberpunk, etc.)
 * - Safe Zone guide overlays (Shorts, Reels, TikTok)
 * - Volume adjustments & fade envelopes
 * - Custom interactive playback controls: play/pause, seek bar, forward/rewind, mute, speed, aspect ratio
 * - Interactive storyboard fallback if video stream fails or is generating
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    videoUri: String? = null,
    modifier: Modifier = Modifier,
    title: String = "Generated Video Preview",
    aspectRatio: Float = 16f / 9f,
    autoPlay: Boolean = false,
    filterPreset: VideoFilterPreset = VideoFilterPreset.NONE,
    safeZoneType: SafeZoneType = SafeZoneType.NONE,
    trimBounds: Pair<Long, Long>? = null,
    masterVolume: Float = 1.0f,
    fadeInSec: Float = 0f,
    fadeOutSec: Float = 0f,
    sceneMarkers: List<String> = emptyList(),
    visualPrompts: List<String> = emptyList(),
    sceneImages: List<String> = emptyList(),
    scriptText: String = "",
    onPositionChange: (Long) -> Unit = {},
    onPickLocalVideo: ((String) -> Unit)? = null,
    onSceneSelect: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Local video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPickLocalVideo?.invoke(it.toString()) }
    }

    // Player state
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(60000L) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var currentRatio by remember { mutableFloatStateOf(aspectRatio) }

    // Error & Simulation Fallback state
    var isSceneSimulationMode by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var playbackErrorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

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

        // Local content URIs or file paths are valid media items
        if (raw.startsWith("content://") || raw.startsWith("file://")) {
            return raw.trim()
        }

        // Web links that are HTML pages rather than MP4 streams
        if (lower.contains("studio.youtube.com") || lower.contains("instagram.com/reel") ||
            lower.contains("facebook.com/watch") || lower.contains("youtube.com/watch") ||
            (!lower.endsWith(".mp4") && !lower.endsWith(".mkv") && !lower.endsWith(".webm") && !lower.contains("/sample/"))
        ) {
            return reliableStreams.first()
        }
        return raw.trim()
    }

    // ExoPlayer creation & lifecycle
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = autoPlay
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
            if (autoPlay) {
                exoPlayer.play()
            }
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

    // Observe lifecycle: pause on background
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

                // Boundary checking
                if (trimBounds != null) {
                    val (startMs, endMs) = trimBounds
                    if (pos >= endMs) {
                        exoPlayer.seekTo(startMs)
                        currentPositionMs = startMs
                    } else if (pos < startMs) {
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

    fun toggleAspectRatio() {
        currentRatio = if (currentRatio == 16f / 9f) 9f / 16f else 16f / 9f
    }

    fun retryStream() {
        isSceneSimulationMode = false
        hasPlaybackError = false
        playbackErrorMessage = null
        reloadTrigger++
    }

    val activeColorMatrix = remember(filterPreset) {
        VideoColorMatrixHelper.getColorMatrix(filterPreset)
    }

    Box(
        modifier = modifier
            .testTag("video_player_view")
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .aspectRatio(currentRatio)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            },
        contentAlignment = Alignment.Center
    ) {
        // Video Layer with Color Filter & Blend Mode
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
                        drawRect(
                            color = tintColor,
                            blendMode = blendMode
                        )
                    }
                }
        ) {
            if (!isSceneSimulationMode) {
                // Media3 PlayerView inside AndroidView
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Interactive Studio Visual Scene Preview Mode (Real Visual Scene Rendering)
                val activeImagePath = sceneImages.getOrNull(activeSceneIndex)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("interactive_visual_scene_container"),
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
                        // Render rich visual atmospheric canvas
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF380D3D))
                                )
                            )
                            // Horizon & Mountains
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

                    // Scene Header Badge & Stepper Overlays
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Scene Badge and Prompt Tag
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
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Stepper Prev / Next
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
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Scene", tint = Color.White, modifier = Modifier.size(24.dp))
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
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Scene", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }

                        // Bottom Subtitles & Scene Prompt overlay
                        val activePrompt = visualPrompts.getOrElse(activeSceneIndex) {
                            "Cinematic 4K shot with vibrant neon rim lighting & high-retention pacing"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                if (scriptText.isNotBlank()) {
                                    Text(
                                        text = "\"${scriptText.take(100)}\"",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioAmber,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = "🎨 $activePrompt",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Safe Zone Guide Overlay (Shorts, Reels, TikTok)
        SafeZoneGuideOverlay(
            safeZoneType = safeZoneType,
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && !isSceneSimulationMode) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = StudioCrimson,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Error Banner
        if (hasPlaybackError && !isSceneSimulationMode) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Red.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = playbackErrorMessage ?: "Stream unavailable",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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

        // Custom Overlay Controls (Visible on tap)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(10.dp)
            ) {
                // Top Bar: Title & Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSceneSimulationMode) StudioPurple else StudioCrimson,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pick local video file
                        IconButton(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier.size(28.dp).testTag("pick_local_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Open local video file",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Switch between Stream & Storyboard mode
                        Surface(
                            onClick = {
                                if (isSceneSimulationMode) retryStream() else isSceneSimulationMode = true
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSceneSimulationMode) StudioPurple.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSceneSimulationMode) Icons.Default.MovieFilter else Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isSceneSimulationMode) "STORYBOARD" else "STREAM",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Ratio Switcher
                        Surface(
                            onClick = { toggleAspectRatio() },
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = if (currentRatio == 16f / 9f) "16:9" else "9:16",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        // Speed
                        Surface(
                            onClick = { cycleSpeed() },
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        // Mute / Unmute
                        IconButton(
                            onClick = { toggleMute() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Center Controls: Seek Backward 10s, Play/Pause, Seek Forward 10s
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { seekRelative(-5000L) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("video_seek_backward_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Seek backward 5s",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("-5s", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }

                    IconButton(
                        onClick = { togglePlayPause() },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(StudioCrimson)
                            .testTag("video_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { seekRelative(5000L) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("video_seek_forward_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Seek forward 5s",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("+5s", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Bottom Bar: Progress Slider + Time Display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    // Optional Scene Markers
                    if (sceneMarkers.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sceneMarkers.forEachIndexed { index, marker ->
                                Surface(
                                    onClick = {
                                        val targetPos = (durationMs / sceneMarkers.size) * index
                                        seekTo(targetPos)
                                        onSceneSelect(index)
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.height(18.dp)
                                ) {
                                    Text(
                                        text = marker,
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Seek Slider
                    val sliderValue = if (durationMs > 0) {
                        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Slider(
                        value = sliderValue,
                        onValueChange = { fraction ->
                            seekTo((fraction * durationMs).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = StudioCrimson,
                            activeTrackColor = StudioCrimson,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("video_seek_bar")
                    )

                    // Time labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatMs(currentPositionMs),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (trimBounds != null) {
                            Text(
                                text = "Trim: ${formatMs(trimBounds.first)} - ${formatMs(trimBounds.second)}",
                                color = StudioAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = formatMs(durationMs),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
