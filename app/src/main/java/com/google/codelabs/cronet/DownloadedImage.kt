package com.google.codelabs.cronet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DownloadedImage(
    val url: String
) {
    var downloaderResult: ImageDownloaderResult? by mutableStateOf(null)
}
