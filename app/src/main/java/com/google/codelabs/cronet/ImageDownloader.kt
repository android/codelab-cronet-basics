package com.google.codelabs.cronet

interface ImageDownloader {
    suspend fun downloadImage(url: String): ImageDownloaderResult
}