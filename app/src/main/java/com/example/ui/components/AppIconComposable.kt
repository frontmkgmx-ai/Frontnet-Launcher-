package com.example.ui.components

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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.IconShape
import com.example.model.LauncherApp
import com.example.util.AppIconCache

/**
 * High-performance AppIcon composable.
 * Reads pre-decoded ImageBitmap directly from memory cache or AppItem/LauncherApp.
 * NEVER calls PackageManager or performs disk/IPC operations on the UI thread!
 */
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
    // 1. Direct ImageBitmap lookup from model, or in-memory LruCache
    val readyImageBitmap: ImageBitmap? = app.icon ?: AppIconCache.getCachedImageBitmap(app.packageName)

    val shape = remember(iconShape) {
        when (iconShape) {
            IconShape.CIRCLE -> CircleShape
            IconShape.SQUIRCLE -> RoundedCornerShape(30)
            IconShape.ROUNDED_SQUARE -> RoundedCornerShape(22)
            IconShape.TEARDROP -> RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 50,
                bottomStartPercent = 50,
                bottomEndPercent = 12
            )
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
                .then(
                    if (iconThemed) {
                        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                    } else if (readyImageBitmap == null && app.iconVector == null) {
                        Modifier.background(app.iconTint?.copy(alpha = 0.9f) ?: MaterialTheme.colorScheme.surfaceVariant)
                    } else {
                        Modifier
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
                } else if (readyImageBitmap != null) {
                    Image(
                        bitmap = readyImageBitmap,
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
                // Vibrant Pre-decoded Icon
                if (readyImageBitmap != null) {
                    Image(
                        bitmap = readyImageBitmap,
                        contentDescription = app.label,
                        modifier = Modifier.size(iconSizeDp.dp).clip(shape)
                    )
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
                lineHeight = 12.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

/**
 * Dedicated AppItem variant of the icon composable.
 * Guaranteed 0ms latency with pre-decoded ImageBitmap.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItemIconComposable(
    item: AppItem,
    iconShape: IconShape,
    iconSizeDp: Int = 56,
    showLabel: Boolean = true,
    labelColor: Color = Color.White,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shape = remember(iconShape) {
        when (iconShape) {
            IconShape.CIRCLE -> CircleShape
            IconShape.SQUIRCLE -> RoundedCornerShape(30)
            IconShape.ROUNDED_SQUARE -> RoundedCornerShape(22)
            IconShape.TEARDROP -> RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 50,
                bottomStartPercent = 50,
                bottomEndPercent = 12
            )
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
            .testTag("app_item_${item.packageName}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSizeDp.dp)
                .shadow(elevation = 4.dp, shape = shape, clip = false)
                .clip(shape)
        ) {
            Image(
                bitmap = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(iconSizeDp.dp).clip(shape)
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.label,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
