package com.gadgeski.vetro.ui.screens

import android.content.Context
import android.graphics.BlurMaskFilter
import android.os.BatteryManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gadgeski.vetro.ui.viewmodel.ClockViewModel
import com.gadgeski.vetro.util.HingePosture
import com.gadgeski.vetro.util.rememberHingePosture
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// Cyberpunk Color Palette
val NeonCyan = Color(0xFF00FFFF)
val HotPink = Color(0xFFFF00FF)
val DeepBlack = Color(0xFF050505)
val HudGray = Color(0xFF333333)

@Composable
fun CyberpunkClockScreen(
    viewModel: ClockViewModel = hiltViewModel()
) {
    val currentTime by viewModel.currentTime.collectAsState()
    val hingePosture by rememberHingePosture()

    // 背景: ディープブラック
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // 背景エフェクト: グリッドとスキャンライン
        CyberpunkBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // --- 上部セクション (時計) ---
            // 半開き時はここがメイン表示になる
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, HudGray.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CockpitClock(currentTime)

                // 装飾: 四隅のコーナーフレーム
                CornerFrame(Modifier.matchParentSize())
            }

            // --- ヒンジエリア (折り目) ---
            // 半開き時はスペーサーとして機能
            if (hingePosture == HingePosture.HALF_OPENED) {
                HingeSpacer()
            }

            // --- 下部セクション (ダッシュボード) ---
            // 半開き時は机に接地する部分
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                DashboardPanel()
            }
        }
    }
}

@Composable
fun CockpitClock(time: LocalTime) {
    val hour = time.format(DateTimeFormatter.ofPattern("HH"))
    val minute = time.format(DateTimeFormatter.ofPattern("mm"))
    val second = time.format(DateTimeFormatter.ofPattern("ss"))
    val date = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // SYSTEM TIME ラベル (点滅)
        BlinkingText(
            text = "SYSTEM TIME",
            color = NeonCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // メイン時計 (ネオン発光)
        Row(verticalAlignment = Alignment.Bottom) {
            NeonText(
                text = hour,
                fontSize = 80.dp, // 固定サイズで一旦実装
                color = NeonCyan
            )
            NeonText(
                text = ":",
                fontSize = 80.dp,
                color = NeonCyan,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            NeonText(
                text = minute,
                fontSize = 80.dp,
                color = NeonCyan
            )
        }

        // 日付と秒
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date,
                color = NeonCyan.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily // BBH Bartle流用
            )
            Text(
                text = "SEC.$second",
                color = HotPink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DashboardPanel() {
    Column {
        Text(
            text = "STATUS MONITOR",
            color = HudGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        HorizontalDivider(color = HudGray)
        Spacer(modifier = Modifier.height(8.dp))

        // バッテリー情報
        BatteryStatusRow()

        Spacer(modifier = Modifier.height(16.dp))

        // システムログ (流れるテキスト)
        SystemLogView()
    }
}

@Composable
fun BatteryStatusRow() {
    val context = LocalContext.current
    val batteryLevel = remember { getBatteryLevel(context) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "PWR", color = NeonCyan, modifier = Modifier.width(40.dp))
        LinearProgressIndicator(
            progress = { batteryLevel / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = if (batteryLevel > 20) NeonCyan else HotPink,
            trackColor = HudGray
        )
        Text(
            text = "${batteryLevel}%",
            color = NeonCyan,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SystemLogView() {
    val logs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    // ダミーログ生成
    LaunchedEffect(Unit) {
        while (true) {
            val hex = Random.nextInt(0, 999999).toString(16).uppercase().padStart(6, '0')
            val msg = listOf("SYNC...", "CHECKING...", "OK", "DATA FLOW", "PING").random()
            logs.add("[$hex] :: $msg")
            if (logs.size > 20) logs.removeAt(0)
            listState.animateScrollToItem(logs.size - 1)
            delay(Random.nextLong(200, 800))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(logs) { log ->
            Text(
                text = log,
                color = NeonCyan.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

// --- Visual Effects ---

@Composable
fun NeonText(text: String, fontSize: Dp, color: Color, modifier: Modifier = Modifier) {
    val paint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            // ネオンの光彩 (Glow)
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        }
    }
    val density = LocalDensity.current
    val textSizePx = with(density) { fontSize.toPx() }

    Box(modifier = modifier) {
        // 背面の光彩
        Canvas(modifier = Modifier.matchParentSize()) {
            drawIntoCanvas {
                paint.color = color.toArgb()
                paint.textSize = textSizePx
                // 中央配置のための簡易計算（厳密ではないが雰囲気重視）
                // 【修正】 nativeCanvas にアクセスするために必要なインポートを追加しました
                it.nativeCanvas.drawText(text, 0f, size.height * 0.8f, paint)
            }
        }
        // 前面のくっきりした文字
        Text(
            text = text,
            fontSize = with(density) { fontSize.toSp() },
            color = Color.White, // 中心は白く光る
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.alpha(0.9f)
        )
    }
}

@Composable
fun CyberpunkBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineY"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 1. 薄いグリッド
        val gridSize = 50.dp.toPx()
        val width = size.width
        val height = size.height

        for (x in 0..width.toInt() step gridSize.toInt()) {
            drawLine(
                color = HudGray,
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), height),
                strokeWidth = 1f,
                alpha = 0.2f
            )
        }
        for (y in 0..height.toInt() step gridSize.toInt()) {
            drawLine(
                color = HudGray,
                start = Offset(0f, y.toFloat()),
                end = Offset(width, y.toFloat()),
                strokeWidth = 1f,
                alpha = 0.2f
            )
        }

        // 2. スキャンライン
        val lineHeight = height * 0.1f
        val yPos = height * scanlineY
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.1f), Color.Transparent),
                startY = yPos,
                endY = yPos + lineHeight
            ),
            topLeft = Offset(0f, yPos),
            size = size.copy(height = lineHeight)
        )
    }
}

@Composable
fun BlinkingText(text: String, color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Text(text = text, color = color.copy(alpha = alpha), modifier = modifier, letterSpacing = 2.sp)
}

@Composable
fun CornerFrame(modifier: Modifier) {
    val color = NeonCyan.copy(alpha = 0.5f)
    val length = 20.dp
    val thickness = 2.dp

    Canvas(modifier = modifier) {
        // 左上
        drawLine(color, Offset(0f, 0f), Offset(length.toPx(), 0f), thickness.toPx())
        drawLine(color, Offset(0f, 0f), Offset(0f, length.toPx()), thickness.toPx())
        // 右上
        drawLine(color, Offset(size.width, 0f), Offset(size.width - length.toPx(), 0f), thickness.toPx())
        drawLine(color, Offset(size.width, 0f), Offset(size.width, length.toPx()), thickness.toPx())
        // 左下
        drawLine(color, Offset(0f, size.height), Offset(length.toPx(), size.height), thickness.toPx())
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - length.toPx()), thickness.toPx())
        // 右下
        drawLine(color, Offset(size.width, size.height), Offset(size.width - length.toPx(), size.height), thickness.toPx())
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - length.toPx()), thickness.toPx())
    }
}

@Composable
fun HingeSpacer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp) // ヒンジ部分の高さ
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "--- FOLD AXIS ---",
            color = HudGray,
            fontSize = 10.sp,
            letterSpacing = 4.sp
        )
    }
}

// 【削除】 独自のModifier.width拡張関数は削除しました。
// 標準ライブラリの androidx.compose.foundation.layout.width を使用します。

// バッテリー取得Util
fun getBatteryLevel(context: Context): Int {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}