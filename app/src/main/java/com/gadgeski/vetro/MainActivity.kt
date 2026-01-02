package com.gadgeski.vetro

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gadgeski.vetro.ui.screens.CyberpunkClockScreen
import com.gadgeski.vetro.ui.theme.VetroTheme
import dagger.hilt.android.AndroidEntryPoint

// 新しいサイバーパンク画面(import com.gadgeski.vetro.ui.screens.CyberpunkClockScreen)
// import com.gadgeski.vetro.ui.screens.DeskClockScreen
// 元の時計画面（戻すときはコメントアウトを解除）

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge 有効化
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        // 常時点灯モード
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            VetroTheme {
                // 作成した時計画面を表示

                // 通常モード（ミニマル）
                // DeskClockScreen()

                // 実験モード（サイバーパンク）
                CyberpunkClockScreen()
            }
        }
    }
}