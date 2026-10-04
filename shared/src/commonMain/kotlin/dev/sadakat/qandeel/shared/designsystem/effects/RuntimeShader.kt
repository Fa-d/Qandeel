package dev.sadakat.qandeel.shared.designsystem.effects

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A fragment shader compiled at runtime (AGSL on Android, SkSL through Skia on iOS and the JVM; the
 * sources are written in what both accept). Set its uniforms, then draw with [brush]; set them
 * again before each frame.
 */
internal interface RuntimeShader {
    fun uniform(name: String, value: Float)

    fun uniform(name: String, x: Float, y: Float)

    fun uniform(name: String, color: Color)

    /** A brush that paints the shader with the uniforms as they are now. */
    fun brush(): Brush
}

/** [source] compiled, or null where this platform has no runtime shaders (Android before 13). */
internal expect fun runtimeShaderOrNull(source: String): RuntimeShader?
