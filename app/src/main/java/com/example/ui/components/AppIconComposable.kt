package com.example.ui.components

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.model.IconShape
import com.example.model.LauncherApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconComposable(
    app: LauncherApp,
    iconShape: IconShape,
    iconSizeDp: Int = 56,
    iconThemed: Boolean = false,
    showLabel: Boolean = true,
    labelColor: Color = Color.White,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = remember(iconShape) {
        when (iconShape) {
            IconShape.CIRCLE -> CircleShape
            IconShape.SQUIRCLE -> RoundedCornerShape(30)
            IconShape.ROUNDED_SQUARE -> RoundedCornerShape(22)
            IconShape.TEARDROP -> RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 50, bottomEndPercent = 12)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width((iconSizeDp + 24).dp)
            .padding(horizontal = 2.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("app_icon_${app.packageName}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSizeDp.dp)
                .shadow(
                    elevation = if (iconThemed) 2.dp else 4.dp,
                    shape = shape,
                    clip = false
                )
                .clip(shape)
                .background(
                    if (iconThemed) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        app.iconTint?.copy(alpha = 0.9f) ?: MaterialTheme.colorScheme.surfaceVariant
                    }
                )
        ) {
            if (iconThemed) {
                // Material You Monochrome Themed Icon
                if (app.iconVector != null) {
                    Icon(
                        imageVector = app.iconVector,
                        contentDescription = app.label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size((iconSizeDp * 0.55).dp)
                    )
                } else if (app.iconDrawable != null) {
                    val bitmap = remember(app.iconDrawable) {
                        drawableToImageBitmap(app.iconDrawable)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.label,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                            modifier = Modifier.size((iconSizeDp * 0.58).dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Android,
                            contentDescription = app.label,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size((iconSizeDp * 0.55).dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Android,
                        contentDescription = app.label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size((iconSizeDp * 0.55).dp)
                    )
                }
            } else {
                // Vibrant Original Icon
                if (app.iconDrawable != null) {
                    val bitmap = remember(app.iconDrawable) {
                        drawableToImageBitmap(app.iconDrawable)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size((iconSizeDp * 0.72).dp)
                        )
                    } else {
                        Icon(
                            imageVector = app.iconVector ?: Icons.Rounded.Android,
                            contentDescription = app.label,
                            tint = Color.White,
                            modifier = Modifier.size((iconSizeDp * 0.55).dp)
                        )
                    }
                } else if (app.iconVector != null) {
                    Icon(
                        imageVector = app.iconVector,
                        contentDescription = app.label,
                        tint = Color.White,
                        modifier = Modifier.size((iconSizeDp * 0.55).dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Android,
                        contentDescription = app.label,
                        tint = Color.White,
                        modifier = Modifier.size((iconSizeDp * 0.55).dp)
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

private fun drawableToImageBitmap(drawable: Drawable): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            is ColorDrawable -> {
                val bmp = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                drawable.draw(canvas)
                bmp
            }
            else -> {
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
                drawable.toBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            }
        }
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
