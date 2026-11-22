package edu.bu.seatscout

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp

internal class DockBackdrop(val scene: GraphicsLayer, val lens: GraphicsLayer) {
    var sceneOrigin = Offset.Zero
    var lensOrigin = Offset.Zero
}

internal fun Modifier.recordDockBackdrop(backdrop: DockBackdrop): Modifier =
    onGloballyPositioned { backdrop.sceneOrigin = it.positionInRoot() }
        .drawWithContent {
            if (!drawContext.canvas.nativeHardwareAccelerated()) {
                drawContent()
            } else {
                backdrop.scene.record { this@drawWithContent.drawContent() }
                drawLayer(backdrop.scene)
            }
        }

internal fun Modifier.dockGlass(backdrop: DockBackdrop?, surface: Color, dark: Boolean): Modifier =
    onGloballyPositioned { if (backdrop != null) backdrop.lensOrigin = it.positionInRoot() }
        .drawWithCache {
            val blur = 14.dp.toPx()
            if (backdrop != null && Build.VERSION.SDK_INT >= 31) {
                val frost = AndroidRenderEffect.createBlurEffect(blur, blur, Shader.TileMode.CLAMP)
                val effect = if (Build.VERSION.SDK_INT >= 33 && size.width > 0 && size.height > 0) {
                    DockOptics.create(frost, size.width, size.height, 28.dp.toPx(), 4.dp.toPx())
                } else frost
                backdrop.lens.renderEffect = effect.asComposeRenderEffect()
            }
            val sheen = Brush.verticalGradient(listOf(Color.White.copy(alpha = if (dark) .07f else .30f), Color.Transparent))
            onDrawBehind {
                val canSample = backdrop != null && backdrop.scene.size.width > 0 && drawContext.canvas.nativeHardwareAccelerated()
                if (canSample) {
                    // Sample the scrolling scene at the dock's position without taking a bitmap snapshot.
                    val shift = backdrop.sceneOrigin - backdrop.lensOrigin
                    backdrop.lens.record { translate(shift.x, shift.y) { drawLayer(backdrop.scene) } }
                    drawLayer(backdrop.lens)
                }
                drawRect(surface.copy(alpha = if (!canSample) 1f else if (dark) .86f else .79f))
                drawRect(sheen)
            }
        }

private fun androidx.compose.ui.graphics.Canvas.nativeHardwareAccelerated(): Boolean =
    nativeCanvas.isHardwareAccelerated

@RequiresApi(33)
private object DockOptics {
    fun create(blur: AndroidRenderEffect, width: Float, height: Float, radius: Float, strength: Float): AndroidRenderEffect {
        val shader = RuntimeShader(
            """
            uniform shader content;
            uniform float2 size;
            uniform float radius;
            uniform float strength;

            half4 main(float2 p) {
                float2 local = p - size * 0.5;
                float2 q = abs(local) - size * 0.5 + radius;
                float distance = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
                float rim = smoothstep(-radius * 0.62, 0.0, distance);
                float2 towardCenter = -local / max(length(local), 1.0);
                float2 bent = clamp(p + towardCenter * strength * rim, float2(0.5), size - 0.5);
                return content.eval(bent);
            }
            """.trimIndent()
        )
        shader.setFloatUniform("size", width, height)
        shader.setFloatUniform("radius", radius)
        shader.setFloatUniform("strength", strength)
        return AndroidRenderEffect.createChainEffect(AndroidRenderEffect.createRuntimeShaderEffect(shader, "content"), blur)
    }
}
