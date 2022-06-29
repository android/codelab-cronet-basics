package com.google.codelabs.cronet

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.time.Duration

internal class NativeImageDownloader: ImageDownloader {
    // The blocking suspend fun is always executed from an IO scope. There's no native i/o support
    // for coroutines, we intentionally demonstrate the state before using Cronet.
    @Suppress("BlockingMethodInNonBlockingContext")
     override suspend fun downloadImage(urlString: String): ImageDownloaderResult {
         try {
             val before: Long = System.nanoTime()
             val url = URL(urlString)
             val bytesReceived = ByteArrayOutputStream()
             url.openStream().use { it.transferTo(bytesReceived) }
             return ImageDownloaderResult(
                 successful = true,
                 blob = bytesReceived.toByteArray(),
                 latency = Duration.ofNanos(System.nanoTime() - before),
                 wasCached = false,
                downloaderRef = this)
         } catch (e: IOException) {
             return ImageDownloaderResult(
                 successful = false,
                 blob = ByteArray(0),
                 latency = Duration.ZERO,
                 wasCached = false,
                 downloaderRef = this
             )
         }
    }

    override fun toString() : String {
        return "Native Image Downloader"
    }
}

// Backport of Java 9's transferTo method.
private fun InputStream.transferTo(dest: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (read(buf).also { length = it } > 0) {
        dest.write(buf, 0, length)
    }
}
