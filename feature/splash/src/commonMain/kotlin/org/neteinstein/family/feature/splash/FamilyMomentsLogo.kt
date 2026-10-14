package org.neteinstein.family.feature.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.painterResource
import org.neteinstein.family.ui.resources.Res
import org.neteinstein.family.ui.resources.ic_campfire_ember_large
import org.neteinstein.family.ui.resources.ic_campfire_ember_small
import org.neteinstein.family.ui.resources.ic_campfire_flame
import org.neteinstein.family.ui.resources.ic_campfire_scene_base

/**
 * Prefetches the campfire artwork's raw bytes before [FamilyMomentsLogo] is first drawn.
 * `painterResource()` loads synchronously on Android/iOS, but on the web target it loads
 * resources asynchronously - the first composition gets an empty painter, replaced once the
 * fetch/decode resolves (see the Compose Multiplatform resources library). [SplashScreen]'s
 * entrance animation and auto-navigate timer aren't otherwise aware of that load, so on a slow or
 * cold-cache web load the screen could finish and navigate to Home before the logo was ever
 * visibly drawn - reported as "the splash icon is missing" on web. Awaiting this first warms the
 * resource loader (including the browser's own HTTP cache) so the actual `painterResource()` calls
 * inside [FamilyMomentsLogo] resolve immediately once this returns; a fast no-op on Android/iOS,
 * where reads are already synchronous.
 */
suspend fun preloadFamilyMomentsLogo(environment: ResourceEnvironment) {
    val drawables: List<DrawableResource> =
        listOf(
            Res.drawable.ic_campfire_scene_base,
            Res.drawable.ic_campfire_flame,
            Res.drawable.ic_campfire_ember_large,
            Res.drawable.ic_campfire_ember_small,
        )
    coroutineScope {
        drawables.map { async { getDrawableResourceBytes(environment, it) } }.forEach { it.await() }
    }
}

/**
 * Renders the same campfire-scene artwork used by the launcher icon, so the splash screen matches
 * it - but with the flame flickering and its embers drifting upward, since a still image can't
 * show that on the launcher icon itself.
 */
@Composable
fun FamilyMomentsLogo(modifier: Modifier = Modifier) {
    val flicker = rememberInfiniteTransition(label = "campfireFlicker")

    // Two overlapping periods (650ms/900ms) so the combined motion doesn't look like a metronome.
    val flameScale by flicker.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "flameScale",
    )
    val flameSkew by flicker.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "flameSkew",
    )
    val flameAlpha by flicker.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "flameAlpha",
    )

    // 0f..1f progress each ember rides from the fire up into the air, then restarts from the fire.
    val emberLargeProgress by flicker.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "emberLargeProgress",
    )
    val emberSmallProgress by flicker.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                initialStartOffset = StartOffset(600),
            ),
        label = "emberSmallProgress",
    )

    Box(modifier = modifier) {
        Image(
            painter = painterResource(Res.drawable.ic_campfire_scene_base),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )
        Image(
            painter = painterResource(Res.drawable.ic_campfire_flame),
            contentDescription = null,
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        // Pivot near the flame's base (54,76 of the 108x108 viewport) so it grows
                        // upward from the logs instead of swelling from the icon's center.
                        transformOrigin = TransformOrigin(0.5f, 0.70f)
                        scaleY = flameScale
                        scaleX = 1f - (flameScale - 1f) * 0.4f
                        rotationZ = flameSkew
                        alpha = flameAlpha
                    },
        )
        Image(
            painter = painterResource(Res.drawable.ic_campfire_ember_large),
            contentDescription = null,
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer { driftEmber(progress = emberLargeProgress, baseAlpha = 0.85f) },
        )
        Image(
            painter = painterResource(Res.drawable.ic_campfire_ember_small),
            contentDescription = null,
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer { driftEmber(progress = emberSmallProgress, baseAlpha = 0.7f) },
        )
    }
}

/** Drifts an ember up and slightly sideways as it rises, fading and growing it out near the top. */
private fun GraphicsLayerScope.driftEmber(
    progress: Float,
    baseAlpha: Float,
) {
    translationY = -size.height * 0.16f * progress
    translationX = size.width * 0.02f * progress
    alpha = baseAlpha * (1f - progress)
    val scale = 1f + progress * 0.3f
    scaleX = scale
    scaleY = scale
}
