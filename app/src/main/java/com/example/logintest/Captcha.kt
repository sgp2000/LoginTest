package com.example.logintest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/**
 * 图形验证码：随机生成「数字 + 字母」组合，带字符旋转、颜色差异与干扰线/噪点，
 * 每次登录失败或点击图片都会刷新。
 */
object Captcha {

    /** 去掉易混淆字符：0 O 1 I */
    private const val CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    const val LENGTH = 4

    /** 生成一个新的验证码字符串 */
    fun generate(random: Random = Random.Default): String =
        buildString { repeat(LENGTH) { append(CHARS[random.nextInt(CHARS.length)]) } }

    /** 校验用户输入（忽略大小写与首尾空格） */
    fun matches(input: String, expected: String): Boolean =
        input.trim().equals(expected, ignoreCase = true)
}

/** 字符颜色池 */
private val INK_COLORS = listOf(
    Color(0xFF185FA5), // 蓝
    Color(0xFF0F6E56), // 墨绿
    Color(0xFF993C1D), // 砖红
    Color(0xFF534AB7), // 紫
    Color(0xFF854F0B), // 棕
    Color(0xFFA32D2D)  // 红
)

/** 干扰线颜色池（半透明） */
private val LINE_COLORS = listOf(
    Color(0x5585B7EB),
    Color(0x5597C459),
    Color(0x55F0997B),
    Color(0x55AFA9EC)
)

/** 单个字符的绘制参数（由验证码决定，保证重组时不变） */
private data class Glyph(val char: Char, val color: Color, val angle: Float, val dy: Dp)

/** 一条干扰线的相对坐标（0~1） */
private data class NoiseLine(
    val x1: Float, val y1: Float, val x2: Float, val y2: Float, val color: Color
)

private data class NoiseDot(val x: Float, val y: Float, val r: Float, val color: Color)

@Composable
fun CaptchaImage(
    code: String,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit
) {
    val glyphs = remember(code) {
        val rnd = Random(code.hashCode())
        code.map { c ->
            Glyph(
                char = c,
                color = INK_COLORS[rnd.nextInt(INK_COLORS.size)],
                angle = rnd.nextInt(-24, 25).toFloat(),
                dy = rnd.nextInt(-4, 5).dp
            )
        }
    }

    val lines = remember(code) {
        val rnd = Random(code.hashCode() + 17)
        List(5) {
            NoiseLine(
                x1 = rnd.nextFloat(), y1 = rnd.nextFloat(),
                x2 = rnd.nextFloat(), y2 = rnd.nextFloat(),
                color = LINE_COLORS[rnd.nextInt(LINE_COLORS.size)]
            )
        }
    }

    val dots = remember(code) {
        val rnd = Random(code.hashCode() + 91)
        List(30) {
            NoiseDot(
                x = rnd.nextFloat(), y = rnd.nextFloat(),
                r = 1f + rnd.nextFloat() * 1.6f,
                color = if (rnd.nextBoolean()) Color(0x4D5F5E5A) else Color(0x4D888780)
            )
        }
    }

    Box(
        modifier = modifier
            .width(124.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1EFE8))
            .clickable { onRefresh() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            dots.forEach { d ->
                drawCircle(d.color, d.r, Offset(d.x * size.width, d.y * size.height))
            }
            lines.forEach { l ->
                drawLine(
                    color = l.color,
                    start = Offset(l.x1 * size.width, l.y1 * size.height),
                    end = Offset(l.x2 * size.width, l.y2 * size.height),
                    strokeWidth = 1.6f
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            glyphs.forEach { g ->
                Text(
                    text = g.char.toString(),
                    color = g.color,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .offset(y = g.dy)
                        .rotate(g.angle)
                )
            }
        }
    }
}
