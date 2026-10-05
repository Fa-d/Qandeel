package dev.sadakat.qandeel.shared.designsystem.effects

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.CelestialClock
import dev.sadakat.qandeel.shared.designsystem.SkyColors
import kotlin.math.sin
import kotlin.random.Random

/**
 * The sky behind every screen. [scroll] (px, read while drawing) moves the star planes against
 * each other; [glowCenter] (fractions of the size) is where the lamp's warmth gathers.
 *
 * Drawn by [SKY_SHADER] where the platform runs shaders, otherwise (Android before 13) by
 * [drawSkyFallback]: the same gradient, glow and stars, without the nebula.
 */
@Composable
fun CelestialSky(
    modifier: Modifier = Modifier,
    scroll: () -> Float = { 0f },
    glowCenter: Offset = Offset(0.5f, 0.3f),
) {
    val colors = Celestial.colors.sky
    val clock = Celestial.clock
    val shader = remember { runtimeShaderOrNull(SKY_SHADER) }
    Spacer(
        modifier.drawBehind {
            if (shader != null) {
                drawSkyShader(shader, colors, clock, scroll(), glowCenter)
            } else {
                drawSkyFallback(colors, clock.seconds(), glowCenter)
            }
        },
    )
}

private fun DrawScope.drawSkyShader(
    shader: RuntimeShader,
    colors: SkyColors,
    clock: CelestialClock,
    scroll: Float,
    glowCenter: Offset,
) {
    shader.uniform("resolution", size.width, size.height)
    shader.uniform("time", clock.seconds())
    shader.uniform("scroll", scroll)
    shader.uniform("starAmount", 1f)
    shader.uniform("skyTop", colors.top)
    shader.uniform("skyBottom", colors.bottom)
    shader.uniform("nebula", colors.nebula)
    shader.uniform("glow", colors.glow)
    shader.uniform("star", colors.star)
    shader.uniform("glowCenter", glowCenter.x * size.width, glowCenter.y * size.height)
    drawRect(shader.brush())
}

/** Star positions (fractions of the size), sizes and phases: fixed, so the sky never reshuffles. */
private val fallbackStars = Random(seed = 7).let { random ->
    List(FALLBACK_STARS) { FallbackStar(random.nextFloat(), random.nextFloat(), random.nextFloat()) }
}

private class FallbackStar(val x: Float, val y: Float, val seed: Float)

internal fun DrawScope.drawSkyFallback(colors: SkyColors, seconds: Float, glowCenter: Offset) {
    drawRect(Brush.verticalGradient(listOf(colors.top, colors.bottom)))
    val center = Offset(glowCenter.x * size.width, glowCenter.y * size.height)
    val reach = size.minDimension * 0.9f
    drawCircle(
        Brush.radialGradient(listOf(colors.glow, Color.Transparent), center = center, radius = reach),
        radius = reach,
        center = center,
    )
    val unit = size.minDimension / 400f
    fallbackStars.forEach { star ->
        val twinkle = 0.55f + 0.45f * sin(seconds * (0.6f + 2.2f * star.seed) + star.seed * 40f)
        drawCircle(
            color = colors.star.copy(alpha = colors.star.alpha * twinkle * (0.35f + 0.65f * star.seed)),
            radius = unit * (0.5f + 1.1f * star.seed),
            center = Offset(star.x * size.width, star.y * size.height),
        )
    }
}

private const val FALLBACK_STARS = 140
