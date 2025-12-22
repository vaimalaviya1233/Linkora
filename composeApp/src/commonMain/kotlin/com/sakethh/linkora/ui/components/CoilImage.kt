package com.sakethh.linkora.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun CoilImage(
    modifier: Modifier,
    imgURL: String,
    userAgent: String,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center
) {
    val platformContext = LocalPlatformContext.current
    AsyncImage(
        model = ImageRequest.Builder(platformContext).data(imgURL)
            .httpHeaders(headers = NetworkHeaders.Builder().add("User-Agent", userAgent).build())
            .crossfade(true).build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment
    )
}