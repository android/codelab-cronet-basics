package com.google.codelabs.cronet

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.codelabs.cronet.CronetCodelabConstants.URLS
import kotlin.random.Random

class ImagesViewModel : ViewModel() {
    val images = mutableStateListOf<DownloadedImage>()

    fun addImage(): Int {
        images.add(DownloadedImage(URLS[Random.nextInt(URLS.size)]))
        return images.size - 1
    }

    fun setDownloaded(index: Int, downloaderResult: ImageDownloaderResult) =
        images[index].let {
            it.downloaderResult = downloaderResult
        }
}
