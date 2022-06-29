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
