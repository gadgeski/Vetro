package com.gadgeski.vetro.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gadgeski.vetro.ui.components.BigTimeDisplay
import com.gadgeski.vetro.ui.viewmodel.ClockViewModel
import com.gadgeski.vetro.util.HingePosture
import com.gadgeski.vetro.util.rememberHingePosture

/**
 * 時計画面のルート
 *
 * - ヒンジ状態（半開きかどうか）を検知し、レイアウトを自動調整します。
 */
@Composable
fun DeskClockScreen(
    viewModel: ClockViewModel = hiltViewModel()
) {
    val currentTime by viewModel.currentTime.collectAsState()

    // ヒンジの状態を監視 (FLAT or HALF_OPENED)
    val hingePosture by rememberHingePosture()

    // 半開き状態なら、画面の「上半分(50%)」だけを使う。通常なら「全部(100%)」使う。
    // アニメーションさせてスムーズに移動させます。
    val heightFraction by animateFloatAsState(
        targetValue = if (hingePosture == HingePosture.HALF_OPENED) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "heightAnimation"
    )

    // 配置ルール:
    // 半開き時 -> 上寄せ (TopCenter) にしないと、折れ目の位置に来てしまう
    // 全開時 -> 中央 (Center)
    val contentAlignment = if (hingePosture == HingePosture.HALF_OPENED) {
        Alignment.TopCenter
    } else {
        Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // 黒背景
        contentAlignment = contentAlignment
    ) {
        // 時計表示エリア
        // heightFraction に合わせて表示領域の高さを変えることで、
        // 「全画面の中央」から「上半分の中央」へとスムーズに移動させます。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(heightFraction), // 1.0f -> 0.5f
            contentAlignment = Alignment.Center
        ) {
            BigTimeDisplay(
                time = currentTime,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}