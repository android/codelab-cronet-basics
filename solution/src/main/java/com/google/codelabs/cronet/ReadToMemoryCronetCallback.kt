package com.google.codelabs.cronet

import android.util.Log
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

internal abstract class ReadToMemoryCronetCallback : UrlRequest.Callback() {
    private val bytesReceived = ByteArrayOutputStream()
    private val receiveChannel = Channels.newChannel(bytesReceived)

    final override fun onRedirectReceived(
        request: UrlRequest, info: UrlResponseInfo?, newLocationUrl: String?
    ) {
        request.followRedirect()
    }

    final override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
        Log.i(TAG, "****** Response Started ******")
        Log.i(TAG, "*** Headers Are *** ${info.allHeaders}")

        // One must use a *direct* byte buffer when calling the read method.
        request.read(ByteBuffer.allocateDirect(BYTE_BUFFER_CAPACITY_BYTES))
    }

    final override fun onReadCompleted(
        request: UrlRequest, info: UrlResponseInfo, byteBuffer: ByteBuffer
    ) {
        // The byte buffer we're getting in the callback hasn't been flipped for reading,
        // so flip it so we can read the content.
        byteBuffer.flip()
        receiveChannel.write(byteBuffer)

        // Reset the buffer to prepare it for the next read
        byteBuffer.clear()

        // Continue reading the request
        request.read(byteBuffer)
    }

    final override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
        val bodyBytes = bytesReceived.toByteArray()

        // We invoke the callback directly here for simplicity. Note that the executor running this
        // callback might be shared with other Cronet requests, or even with other parts of your
        // application. Always make sure to appropriately provision your pools, and consider
        // delegating time consuming work on another executor.
        onSucceeded(request, info, bodyBytes)
    }

    abstract fun onSucceeded(
        request: UrlRequest, info: UrlResponseInfo, bodyBytes: ByteArray)

    companion object {
        private const val TAG = "ReadToMemoryCronetCallback"
        private const val BYTE_BUFFER_CAPACITY_BYTES = 64 * 1024
    }
}