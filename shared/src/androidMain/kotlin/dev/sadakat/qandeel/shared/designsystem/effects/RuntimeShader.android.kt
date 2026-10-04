package dev.sadakat.qandeel.shared.designsystem.effects

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import android.graphics.RuntimeShader as AndroidRuntimeShader

internal actual fun runtimeShaderOrNull(source: String): RuntimeShader? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) AgslShader(source) else null

/** AGSL: the shader object is mutable, so one brush over it follows every uniform change. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class AgslShader(source: String) : RuntimeShader {
    private val shader = AndroidRuntimeShader(source)
    private val brush = ShaderBrush(shader)

    override fun uniform(name: String, value: Float) = shader.setFloatUniform(name, value)

    override fun uniform(name: String, x: Float, y: Float) = shader.setFloatUniform(name, x, y)

    override fun uniform(name: String, color: Color) =
        shader.setFloatUniform(name, color.red, color.green, color.blue, color.alpha)

    override fun brush(): Brush = brush
}
