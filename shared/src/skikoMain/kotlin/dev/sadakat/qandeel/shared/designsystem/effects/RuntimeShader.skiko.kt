package dev.sadakat.qandeel.shared.designsystem.effects

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeShader
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

internal actual fun runtimeShaderOrNull(source: String): RuntimeShader? = SkslShader(source)

/** SkSL through Skia: a built shader is immutable, so each [brush] builds one from the uniforms. */
private class SkslShader(source: String) : RuntimeShader {
    private val builder = RuntimeShaderBuilder(RuntimeEffect.makeForShader(source))

    override fun uniform(name: String, value: Float) = builder.uniform(name, value)

    override fun uniform(name: String, x: Float, y: Float) = builder.uniform(name, x, y)

    override fun uniform(name: String, color: Color) =
        builder.uniform(name, color.red, color.green, color.blue, color.alpha)

    override fun brush(): Brush = ShaderBrush(builder.makeShader().asComposeShader())
}
