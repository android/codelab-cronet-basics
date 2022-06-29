package com.google.codelabs.cronet

interface ImageDownloader {
    suspend fun downloadImage(urlString: String): ImageDownloaderResult
}