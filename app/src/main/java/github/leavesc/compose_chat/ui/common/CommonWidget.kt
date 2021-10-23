package github.leavesc.compose_chat.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * @Author: leavesC
 * @Date: 2021/7/3 18:57
 * @Desc:
 * @Github：https://github.com/leavesC
 */
@Composable
fun SetSystemBarsColor(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
    isLightTheme: Boolean = MaterialTheme.colors.isLight
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = isLightTheme
        )
        systemUiController.setNavigationBarColor(
            color = navigationBarColor,
            darkIcons = isLightTheme
        )
        systemUiController.systemBarsDarkContentEnabled = isLightTheme
    }
}

@Composable
fun CommonDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier, thickness = 0.3.dp,
    )
}

@SuppressLint("ModifierParameter")
@Composable
fun CommonButton(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp), text: String, onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = {
            onClick()
        }) {
        Text(
            text = text
        )
    }
}

@Composable
fun EmptyView() {
    Text(
        text = "Empty",
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.Center),
        style = MaterialTheme.typography.subtitle2,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 49.sp,
    )
}

fun Modifier.scrim(colors: List<Color>): Modifier = drawWithContent {
    drawContent()
    drawRect(Brush.verticalGradient(colors))
}