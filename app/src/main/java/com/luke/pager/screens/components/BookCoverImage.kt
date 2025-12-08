
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter

@Composable
fun BookCoverImage(
    modifier: Modifier = Modifier,
    coverData: ByteArray? = null,
    coverUrl: String? = null,
    cornerRadius: Int = 14,
    width: Dp = 80.dp,
    heightMax: Dp = 120.dp,
    contentScale: ContentScale = ContentScale.Crop
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    var aspectRatio by remember { mutableFloatStateOf(2f / 3f) }

    Box(
        modifier = Modifier
            .width(width)
            .heightIn(max = heightMax)
    ) {
        Box(
            modifier = modifier
                .aspectRatio(aspectRatio)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            when {
                coverData != null -> {
                    var imageBitmap by remember(coverData) { mutableStateOf<ImageBitmap?>(null) }
                    var loading by remember(coverData) { mutableStateOf(true) }

                    LaunchedEffect(coverData) {
                        loading = true
                        val bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.size)
                        imageBitmap = bitmap?.asImageBitmap()
                        loading = false
                        bitmap?.let {
                            if (it.width > 0 && it.height > 0) {
                                aspectRatio = it.width.toFloat() / it.height.toFloat()
                            }
                        }
                    }

                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = "Book Cover",
                            contentScale = contentScale,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Text("No cover", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                coverUrl != null -> {
                    val painter = rememberAsyncImagePainter(model = coverUrl)
                    val painterState = painter.state

                    if (painterState is AsyncImagePainter.State.Success) {
                        val width = painterState.result.drawable.intrinsicWidth
                        val height = painterState.result.drawable.intrinsicHeight
                        if (width > 0 && height > 0) {
                            aspectRatio = width.toFloat() / height.toFloat()
                        }
                    }

                    Image(
                        painter = painter,
                        contentDescription = "Book Cover",
                        contentScale = contentScale,
                        modifier = Modifier.matchParentSize()
                    )

                    when (painterState) {
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }

                        is AsyncImagePainter.State.Error, is AsyncImagePainter.State.Empty -> {
                            Text("No cover", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        else -> Unit
                    }
                }

                else -> {
                    Text("No cover", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
