/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codelabs.cronet

import android.util.Log
import com.google.codelabs.cronet.CronetCodelabConstants.LOGGER_TAG
import kotlinx.coroutines.sync.Semaphore
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class CronetImageDownloader (
    private val engine: CronetEngine) : ImageDownloader {

    private val executor = Executors.newSingleThreadExecutor()

    override suspend fun downloadImage(urlString: String): ImageDownloaderResult {
        val startNanoTime = System.nanoTime()
        return suspendCoroutine {
            cont ->
            val request = engine.newUrlRequestBuilder(urlString, object: ReadToMemoryCronetCallback() {
            override fun onSucceeded(
                request: UrlRequest,
                info: UrlResponseInfo,
                bodyBytes: ByteArray) {
                cont.resume(ImageDownloaderResult(
                    successful = true,
                    blob = bodyBytes,
                    latency = Duration.ofNanos(System.nanoTime() - startNanoTime),
                    wasCached = info.wasCached(),
                    downloaderRef = this@CronetImageDownloader))
            }

            override fun onFailed(
                request: UrlRequest,
                info: UrlResponseInfo,
                error: CronetException
            ) {
                Log.w(LOGGER_TAG, "Cronet download failed!", error)
                cont.resume(ImageDownloaderResult(
                    successful = false,
                    blob = ByteArray(0),
                    latency = Duration.ZERO,
                    wasCached = info.wasCached(),
                    downloaderRef = this@CronetImageDownloader))
            }
        }, executor)

        if (urlString == CronetCodelabConstants.URLS[0]) {
            request.disableCache()
        }

        request.build().start()
        }
    }

    override fun toString() : String {
        return "Cronet Image Downloader"
    }
}