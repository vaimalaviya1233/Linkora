package com.sakethh.linkora.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sakethh.linkora.utils.linkoraLog

@Composable
fun CoilImage(
    modifier: Modifier, imgURL: String, userAgent: String,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    linkoraLog("Url is $imgURL, user agent is :$userAgent")
    AsyncImage(
        model = ImageRequest.Builder(context).data(imgURL).addHeader(
            "User-Agent", userAgent
        ).crossfade(true).build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}