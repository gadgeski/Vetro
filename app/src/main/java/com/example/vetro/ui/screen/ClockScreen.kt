// ClockScreen.kt
package com.example.vetro.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetro.R
import com.example.vetro.ui.components.GlassText
import com.example.vetro.ui.util.rememberSystemWallpaper
import com.example.vetro.ui.viewmodel.ClockViewModel

// 【追加】位置の微調整用(androidx.compose.foundation.layout.offset)
// 【追加】 向きの定数を使うため(android.content.res.Configuration)
// 【追加】 横画面用レイアウト(androidx.compose.foundation.layout.Row)
// 【追加】 横方向のスペース用(androidx.compose.foundation.layout.width)
// 【追加】 画面構成を取得するため(androidx.compose.ui.platform.LocalConfiguration)

@Composable
fun ClockScreen(
    viewModel: ClockViewModel = viewModel()
) {
    val timeString by viewModel.timeString.collectAsState()
    val dateString by viewModel.dateString.collectAsState()

    val systemWallpaper: ImageBitmap? = rememberSystemWallpaper()
    val backgroundImage: ImageBitmap = systemWallpaper ?: ImageBitmap.imageResource(id = R.drawable.background_img)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景描画
        Image(
            bitmap = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isLandscape) {
            // ■ 横画面
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassText(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge,
                    bgImage = backgroundImage,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        } else {
            // ■ 縦画面: ここを画像のように調整します
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // 画面全体の上から 35% くらいの位置に重心を置くイメージ
                    // 全体のバランスを見ながらこの値を調整してください
                    .padding(bottom = 180.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 日付
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    // GlassText側に近づけるため、少し下にずらす
                    modifier = Modifier.offset(y = 240.dp)
                    // 表示位置調整用 下げる:数字を大きく/上げる:数字を小さく、またはマイナスに
                )

                // 時計 (GlassText)
                GlassText(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge,
                    bgImage = backgroundImage,
                    modifier = Modifier
                        // 【変更点】 weight(1f) を削除しました。
                        // これにより、余計な隙間が生まれなくなり、offsetでの距離調整が効きやすくなります。
                        //
                        // 【調整ポイント】
                        // フォントの「上の余白」を打ち消して日付に食い込ませるため、
                        // マイナスの値を大きくしています。使用するフォントに合わせて
                        // -20.dp, -30.dp, -40.dp と調整してみてください。
                        .offset(y = (-20).dp)
                )
            }
        }
    }
}