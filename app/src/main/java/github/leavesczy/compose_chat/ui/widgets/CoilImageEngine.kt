package github.leavesczy.compose_chat.ui.widgets

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import github.leavesczy.matisse.ImageEngine
import kotlinx.parcelize.Parcelize

/**
 * @Author: leavesCZY
 * @Desc:
 */
@Parcelize
class CoilImageEngine : ImageEngine {

    @Composable
    override fun Image(
        modifier: Modifier,
        model: Uri,
        contentScale: ContentScale,
        contentDescription: String?
    ) {
        val context = LocalContext.current
        val request = ImageRequest
            .Builder(context = context)
            .crossfade(enable = false)
            .data(data = model)
            .build()
        AsyncImage(
            modifier = modifier,
            model = request,
            contentScale = contentScale,
            filterQuality = FilterQuality.None,
            contentDescription = contentDescription
        )
    }

}