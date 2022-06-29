package com.google.codelabs.cronet

import java.time.Duration

data class ImageDownloaderResult(
    val successful: Boolean,
    val blob: ByteArray,
    val latency: Duration,
    val wasCached: Boolean,
    val downloaderRef: ImageDownloader
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageDownloaderResult

        if (successful != other.successful) return false
        if (!blob.contentEquals(other.blob)) return false
        if (latency != other.latency) return false
        if (wasCached != other.wasCached) return false

        return true
    }

    override fun hashCode(): Int {
        var result = successful.hashCode()
        result = 31 * result + blob.contentHashCode()
        result = 31 * result + latency.hashCode()
        result = 31 * result + wasCached.hashCode()
        return result
    }
}
