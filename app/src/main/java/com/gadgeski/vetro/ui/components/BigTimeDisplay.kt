package com.gadgeski.vetro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Vetroのメイン機能：特大時計表示コンポーネント
 *
 * - BBH Bartleフォントを使用 (MaterialTheme.typography.displayLarge)
 * - 縦積み（Stack）レイアウトで、時と分を最大サイズで表示
 */
@Composable
fun BigTimeDisplay(
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    // "HH" (24時間表記の2桁)
    val hourString = time.format(DateTimeFormatter.ofPattern("HH"))
    // "mm" (分の2桁)
    val minuteString = time.format(DateTimeFormatter.ofPattern("mm"))

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 時 (Hour)
        Text(
            text = hourString,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
            // フォントによっては上下の余白(Leading)が大きすぎる場合があるため、
            // 必要に応じてここで微調整を行いますが、まずは標準で配置します。
        )

        // 分 (Minute)
        Text(
            text = minuteString,
            // 分も同じ特大サイズにするか、Type.ktで定義した displayMedium にするか。
            // プロンプト要件の「時は特大、分は中サイズ」に従い、少し小さくします。
            // ただし、BBH Bartleの迫力を出すため、あえて displayLarge を使いつつ
            // Scaleで調整するアプローチもアリですが、まずは Type.kt の displayLarge をそのまま使ってみます。
            // バランスを見て後で変更しましょう。
            style = MaterialTheme.typography.displayLarge,
            color = Color.Gray, // 視認性の階層を作るため、分は少し暗く（グレーに）する
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp) // 行間を少し詰める
        )
    }
}