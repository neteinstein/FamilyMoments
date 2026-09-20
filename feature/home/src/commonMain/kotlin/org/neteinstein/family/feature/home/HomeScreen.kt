package org.neteinstein.family.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory
import org.neteinstein.family.feature.home.platform.PlatformBackHandler
import org.neteinstein.family.feature.home.resources.Res
import org.neteinstein.family.feature.home.resources.cancel
import org.neteinstein.family.feature.home.resources.category_all
import org.neteinstein.family.feature.home.resources.category_daily_life
import org.neteinstein.family.feature.home.resources.category_future_dreams
import org.neteinstein.family.feature.home.resources.category_ice_breakers
import org.neteinstein.family.feature.home.resources.category_memories
import org.neteinstein.family.feature.home.resources.category_values
import org.neteinstein.family.feature.home.resources.cd_close
import org.neteinstein.family.feature.home.resources.cd_filter_by_category
import org.neteinstein.family.feature.home.resources.cd_settings
import org.neteinstein.family.feature.home.resources.cd_shuffle_card
import org.neteinstein.family.feature.home.resources.cd_switch_to_grid_view
import org.neteinstein.family.feature.home.resources.cd_switch_to_swipe_view
import org.neteinstein.family.feature.home.resources.hide_card_message
import org.neteinstein.family.feature.home.resources.hide_card_title
import org.neteinstein.family.feature.home.resources.home_app_name
import org.neteinstein.family.feature.home.resources.home_no_cards_subtitle
import org.neteinstein.family.feature.home.resources.home_no_cards_title
import org.neteinstein.family.feature.home.resources.home_subtitle
import org.neteinstein.family.feature.home.resources.home_swipe_hint
import org.neteinstein.family.feature.home.resources.home_take_turns
import org.neteinstein.family.feature.home.resources.home_vertical_swipe_hint
import org.neteinstein.family.feature.home.resources.yes
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD = 100f
private const val VERTICAL_SWIPE_THRESHOLD = 120f
private const val MAX_VERTICAL_NUDGE = 140f
private const val PEEK_REVEAL_RANGE = 300f
private const val PEEK_MIN_SCALE = 0.90f
private const val PEEK_MAX_SCALE = 0.96f
private const val PEEK_MAX_OFFSET_DP = 18f

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var swipeDirection by remember { mutableIntStateOf(0) } // -1 left, +1 right, 0 none
    var fullScreenQuestion by remember { mutableStateOf<Question?>(null) }
    // Keeps showing the last opened question while the close animation fades/scales it out,
    // instead of the content blanking out the instant fullScreenQuestion is cleared.
    var lastFullScreenQuestion by remember { mutableStateOf<Question?>(null) }
    if (fullScreenQuestion != null) {
        lastFullScreenQuestion = fullScreenQuestion
    }
    var isGridView by remember { mutableStateOf(false) }
    var showHideConfirmDialog by remember { mutableStateOf(false) }
    val cardFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    // Lets a physical keyboard (mainly relevant on the web build, where a mouse/trackpad is often
    // the only other input) page through cards the same way a horizontal swipe does - Left/Right
    // only mirrors the "swipe for a new question" hint's direction, not the vertical (focus/hide)
    // gestures. Requesting focus once on entry is enough for the common case (landing on Home);
    // it doesn't get reclaimed after e.g. the category dropdown takes it, matching how most web
    // apps only capture keyboard shortcuts for the page they're currently focused on.
    LaunchedEffect(Unit) {
        cardFocusRequester.requestFocus()
    }

    PlatformBackHandler(enabled = fullScreenQuestion != null) { fullScreenQuestion = null }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .focusRequester(cardFocusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown ||
                            uiState.isLoading ||
                            isGridView ||
                            fullScreenQuestion != null
                        ) {
                            return@onKeyEvent false
                        }
                        when (event.key) {
                            Key.DirectionLeft -> {
                                swipeDirection = 1
                                viewModel.previousQuestion()
                                true
                            }
                            Key.DirectionRight -> {
                                swipeDirection = -1
                                viewModel.nextQuestion()
                                true
                            }
                            else -> false
                        }
                    },
        ) {
            // Decorative background gradient
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.background,
                                        ),
                                    radius = 900f,
                                ),
                        ),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top bar
                HomeTopBar(
                    onShuffleClick = { uiState.questions.randomOrNull()?.let { fullScreenQuestion = it } },
                    shuffleEnabled = uiState.questions.isNotEmpty(),
                    onSettingsClick = onSettingsClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                if (!isGridView) {
                    SwipeHintRow(
                        text = stringResource(Res.string.home_swipe_hint),
                        style = MaterialTheme.typography.labelMedium,
                        trailingIcons = listOf(Icons.AutoMirrored.Filled.ArrowBack, Icons.AutoMirrored.Filled.ArrowForward),
                    )
                    SwipeHintRow(
                        text = stringResource(Res.string.home_vertical_swipe_hint),
                        style = MaterialTheme.typography.labelSmall,
                        trailingIcons = listOf(Icons.Default.KeyboardArrowUp, Icons.Default.KeyboardArrowDown),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (isGridView) {
                    QuestionGrid(
                        questions = uiState.questions,
                        onQuestionClick = { fullScreenQuestion = it },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    QuestionCard(
                        uiState = uiState,
                        swipeDirection = swipeDirection,
                        modifier = Modifier.weight(1f),
                        onSwipeLeft = {
                            swipeDirection = -1
                            viewModel.nextQuestion()
                        },
                        onSwipeRight = {
                            swipeDirection = 1
                            viewModel.previousQuestion()
                        },
                        onSwipeUp = { fullScreenQuestion = uiState.currentQuestion },
                        onSwipeDown = { showHideConfirmDialog = true },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress indicator dots (above) + category filter (bottom left) below,
                // stacked vertically so a wide category label never overlaps the dots.
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isGridView && !uiState.isLoading && uiState.totalQuestions > 0) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ProgressDots(
                                current = uiState.currentIndex,
                                total = uiState.totalQuestions,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CategoryDropdown(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected,
                            modifier = Modifier.align(Alignment.CenterStart),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // View toggle (bottom right corner), hidden while a card is shown full screen.
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewCarousel else Icons.Default.GridView,
                    contentDescription =
                        stringResource(
                            if (isGridView) Res.string.cd_switch_to_swipe_view else Res.string.cd_switch_to_grid_view,
                        ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = fullScreenQuestion != null,
                enter =
                    fadeIn(tween(200)) +
                        expandIn(
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                            expandFrom = Alignment.Center,
                        ) { fullSize -> IntSize(fullSize.width, (fullSize.height * 0.35f).roundToInt()) },
                exit =
                    fadeOut(tween(200)) +
                        shrinkOut(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            shrinkTowards = Alignment.Center,
                        ) { fullSize -> IntSize(fullSize.width, (fullSize.height * 0.35f).roundToInt()) },
                modifier = Modifier.fillMaxSize(),
            ) {
                FullScreenQuestion(
                    question = lastFullScreenQuestion,
                    onClose = { fullScreenQuestion = null },
                    onRandomClick = { uiState.questions.randomOrNull()?.let { fullScreenQuestion = it } },
                    randomEnabled = uiState.questions.isNotEmpty(),
                )
            }
        }
    }

    if (showHideConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showHideConfirmDialog = false },
            title = { Text(stringResource(Res.string.hide_card_title)) },
            text = { Text(stringResource(Res.string.hide_card_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHideConfirmDialog = false
                        viewModel.markCurrentQuestionAsUsed()
                    },
                ) {
                    Text(stringResource(Res.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideConfirmDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun FullScreenQuestion(
    question: Question?,
    onClose: () -> Unit,
    onRandomClick: () -> Unit,
    randomEnabled: Boolean,
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetY > VERTICAL_SWIPE_THRESHOLD) {
                                onClose()
                            }
                            offsetY = 0f
                        },
                        onDragCancel = { offsetY = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount.y
                        },
                    )
                },
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onClose,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.cd_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onRandomClick,
                enabled = randomEnabled,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = stringResource(Res.string.cd_shuffle_card),
                    tint =
                        if (randomEnabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                question?.category?.let { category ->
                    CategoryPill(category = category)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = question?.text ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * 1.2f,
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onShuffleClick: () -> Unit,
    shuffleEnabled: Boolean,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.home_app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.home_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onShuffleClick,
                enabled = shuffleEnabled,
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = stringResource(Res.string.cd_shuffle_card),
                    tint =
                        if (shuffleEnabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(Res.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A hint line followed by a couple of small trailing direction icons (e.g. left/right or up/down
 * arrows) - replaces literal Unicode arrow characters that used to be embedded directly in the
 * translated string, which rendered as "tofu" boxes on the web build (see [QuestionCategory]'s doc
 * comment for why - the same Wasm/Skia emoji-font-fallback gap applies to these).
 */
@Composable
private fun SwipeHintRow(
    text: String,
    style: TextStyle,
    trailingIcons: List<ImageVector>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.width(4.dp))
        trailingIcons.forEach { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun QuestionCard(
    uiState: HomeUiState,
    swipeDirection: Int,
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    // The front card's height is content-driven (varies with question text length), so the peek
    // card behind it mirrors that measured height - otherwise, being centered with much shorter
    // content of its own, it would sit entirely inside the front card's bounds and never show.
    var frontCardHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val cardRotation by animateFloatAsState(
        targetValue = offsetX * 0.04f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardRotation",
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardOffsetY",
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // Peek of the next card in the stack, revealed underneath as the front card is dragged
        // away - gives the deck visual depth instead of a single flat card floating in space.
        if (uiState.questions.size > 1) {
            val peekIndex =
                if (offsetX > 0) {
                    (uiState.currentIndex - 1 + uiState.questions.size) % uiState.questions.size
                } else {
                    (uiState.currentIndex + 1) % uiState.questions.size
                }
            val dragProgress = (abs(offsetX) / PEEK_REVEAL_RANGE).coerceIn(0f, 1f)
            val peekScale = PEEK_MIN_SCALE + (PEEK_MAX_SCALE - PEEK_MIN_SCALE) * dragProgress
            val peekOffsetDp = PEEK_MAX_OFFSET_DP * (1f - dragProgress)
            PeekCard(
                question = uiState.questions[peekIndex],
                scale = peekScale,
                offsetY = peekOffsetDp.dp,
                height = if (frontCardHeightPx > 0) with(density) { frontCardHeightPx.toDp() } else Dp.Unspecified,
            )
        }

        // The incoming card grows in from the peek card's resting scale/position behind the
        // outgoing one, instead of sliding in from off-screen, so it reads as the next card in
        // the stack stepping forward rather than a new card flying in from the side.
        val peekOffsetPx = with(density) { PEEK_MAX_OFFSET_DP.dp.roundToPx() }
        AnimatedContent(
            targetState = uiState.currentQuestion,
            transitionSpec = {
                val enter =
                    scaleIn(tween(350), initialScale = PEEK_MIN_SCALE) +
                        slideInVertically(tween(350)) { peekOffsetPx } +
                        fadeIn(tween(300))
                if (swipeDirection <= 0) {
                    enter togetherWith (slideOutHorizontally(tween(400)) { -it } + fadeOut(tween(300)))
                } else {
                    enter togetherWith (slideOutHorizontally(tween(400)) { it } + fadeOut(tween(300)))
                }
            },
            label = "questionCard",
        ) { question ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .onGloballyPositioned { frontCardHeightPx = it.size.height }
                        .offset { IntOffset(0, cardOffsetY.roundToInt()) }
                        .rotate(cardRotation)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    val isVerticalSwipe = abs(offsetY) > abs(offsetX)
                                    when {
                                        isVerticalSwipe && offsetY < -VERTICAL_SWIPE_THRESHOLD -> onSwipeUp()
                                        isVerticalSwipe && offsetY > VERTICAL_SWIPE_THRESHOLD -> onSwipeDown()
                                        !isVerticalSwipe && offsetX < -SWIPE_THRESHOLD -> onSwipeLeft()
                                        !isVerticalSwipe && offsetX > SWIPE_THRESHOLD -> onSwipeRight()
                                    }
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                onDragCancel = {
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY = (offsetY + dragAmount.y).coerceIn(-MAX_VERTICAL_NUDGE, MAX_VERTICAL_NUDGE)
                                },
                            )
                        },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                brush =
                                    Brush.linearGradient(
                                        colors =
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                            ),
                                    ),
                            ).padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (question == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(Res.string.home_no_cards_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(Res.string.home_no_cards_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CategoryPill(category = question.category)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = question.text,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2f,
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = stringResource(Res.string.home_take_turns),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeekCard(
    question: Question,
    scale: Float,
    offsetY: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)
                .offset(y = offsetY)
                .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                    ),
                            ),
                    ).padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            CategoryPill(category = question.category)
        }
    }
}

@Composable
private fun QuestionGrid(
    questions: List<Question>,
    onQuestionClick: (Question) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(Res.string.home_no_cards_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.home_no_cards_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(questions, key = { it.id }) { question ->
            GridQuestionCard(
                question = question,
                onClick = { onQuestionClick(question) },
            )
        }
    }
}

@Composable
private fun GridQuestionCard(
    question: Question,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.75f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                    ),
                            ),
                    ).padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            // The full CategoryPill label (icon + name) is too wide for a 3-column card and
            // would get clipped by the card's rounded corners, so just show the icon here.
            Icon(
                imageVector = question.category.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopEnd).size(16.dp),
            )
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun categoryLabelRes(category: QuestionCategory): StringResource =
    when (category) {
        is QuestionCategory.IceBreakers -> Res.string.category_ice_breakers
        is QuestionCategory.Memories -> Res.string.category_memories
        is QuestionCategory.Values -> Res.string.category_values
        is QuestionCategory.FutureDreams -> Res.string.category_future_dreams
        is QuestionCategory.DailyLife -> Res.string.category_daily_life
        else -> Res.string.category_ice_breakers
    }

// A plain function (not @Composable) mapping each category to its icon - see QuestionCategory's
// doc comment for why this lives here rather than as a field on the domain model.
private fun QuestionCategory.icon(): ImageVector =
    when (this) {
        is QuestionCategory.IceBreakers -> Icons.Default.Celebration
        is QuestionCategory.Memories -> Icons.Default.PhotoCamera
        is QuestionCategory.Values -> Icons.Default.Favorite
        is QuestionCategory.FutureDreams -> Icons.Default.AutoAwesome
        is QuestionCategory.DailyLife -> Icons.Default.WbSunny
    }

@Composable
private fun categoryDisplayLabel(category: QuestionCategory): String = stringResource(categoryLabelRes(category))

@Composable
private fun CategoryPill(
    category: QuestionCategory,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = category.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = categoryDisplayLabel(category),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
        )
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: QuestionCategory?,
    onCategorySelected: (QuestionCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val allLabel = stringResource(Res.string.category_all)
    // Deliberately unrolled instead of looping over QuestionCategory.all: calling a @Composable
    // function with an argument sourced from a loop/forEach/map variable reproducibly corrupted
    // that argument under this project's exact Kotlin/Compose compiler version (see git history
    // on this file for the failed alternatives). Direct references to the five known singletons
    // sidestep it entirely, and the category list is small and fixed, so unrolling is cheap.
    val iceBreakersLabel = QuestionCategory.IceBreakers to categoryDisplayLabel(QuestionCategory.IceBreakers)
    val memoriesLabel = QuestionCategory.Memories to categoryDisplayLabel(QuestionCategory.Memories)
    val valuesLabel = QuestionCategory.Values to categoryDisplayLabel(QuestionCategory.Values)
    val futureDreamsLabel = QuestionCategory.FutureDreams to categoryDisplayLabel(QuestionCategory.FutureDreams)
    val dailyLifeLabel = QuestionCategory.DailyLife to categoryDisplayLabel(QuestionCategory.DailyLife)
    val categoryLabels = listOf(iceBreakersLabel, memoriesLabel, valuesLabel, futureDreamsLabel, dailyLifeLabel)
    val selectedLabel = categoryLabels.firstOrNull { (category, _) -> category == selectedCategory }?.second ?: allLabel
    val selectedIcon = selectedCategory?.icon()

    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { expanded = true }
                    .onGloballyPositioned { anchorHeightPx = it.size.height }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectedIcon != null) {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(Res.string.cd_filter_by_category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, -with(density) { anchorHeightPx.toDp() }),
            properties = PopupProperties(focusable = true, clippingEnabled = false),
        ) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    expanded = false
                    onCategorySelected(null)
                },
            )
            for ((category, label) in categoryLabels) {
                DropdownMenuItem(
                    text = { Text(label) },
                    leadingIcon = {
                        Icon(imageVector = category.icon(), contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        expanded = false
                        onCategorySelected(category)
                    },
                )
            }
        }
    }
}

// Capped so a category with many cards doesn't lay out (and re-measure on every swipe) an
// unbounded row of dots - this windows dotCount dots around the current page, so this stays a
// small fixed-size row no matter how large total gets. Hand-rolled with Compose Foundation
// (rather than mx.platacard:compose-pager-indicator, which this screen used pre-KMP-migration)
// since that library is Android-only Jetpack Compose, not Compose Multiplatform.
private const val MAX_VISIBLE_DOTS = 7
private val ACTIVE_DOT_SIZE = 8.dp
private val INACTIVE_DOT_SIZE = 6.dp
private val DOT_SPACING = 6.dp

@Composable
private fun ProgressDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return
    // The deck loops through every card in the category (nextQuestion/previousQuestion wrap via
    // modulo), so the indicator's current-dot index is derived from the wrapped index rather
    // than the raw, ever-increasing current.
    val visibleIndex = current % total
    val dotCount = minOf(total, MAX_VISIBLE_DOTS)
    val windowStart = (visibleIndex - dotCount / 2).coerceIn(0, (total - dotCount).coerceAtLeast(0))

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DOT_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(dotCount) { offset ->
            val isActive = windowStart + offset == visibleIndex
            Box(
                modifier =
                    Modifier
                        .size(if (isActive) ACTIVE_DOT_SIZE else INACTIVE_DOT_SIZE)
                        .clip(CircleShape)
                        .background(
                            if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            },
                        ),
            )
        }
    }
}
