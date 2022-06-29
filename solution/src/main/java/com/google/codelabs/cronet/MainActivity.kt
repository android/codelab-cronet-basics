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

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import com.google.codelabs.cronet.ui.theme.CronetCodelabTheme
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.ui.unit.dp
import com.google.codelabs.cronet.CronetCodelabConstants.LOGGER_TAG
import com.google.android.gms.net.CronetProviderInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.chromium.net.CronetEngine
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

class MainActivity : ComponentActivity() {
    private var imageDownloader: AtomicReference<ImageDownloader> = AtomicReference(NativeImageDownloader())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initImageDownloader(this)
        setContent {
            CronetCodelabTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainDisplay(imageDownloader::get)
                }
            }
        }
    }

    private fun initImageDownloader(ctx: Context) {
        CronetProviderInstaller.installProvider(ctx).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(LOGGER_TAG, "Successfully installed Play Services provider: $it")
                val cronetEngine = CronetEngine.Builder(ctx)
                    .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 10 * 1024 * 1024)
                    .build()
                imageDownloader.set(CronetImageDownloader(cronetEngine))
            } else {
                Log.w(LOGGER_TAG, "Unable to load Cronet from Play Services", it.exception)
            }
        }
    }
}

@Composable
fun MainDisplay(
    imageDownloader: Supplier<ImageDownloader>,
    imagesViewModel: ImagesViewModel = viewModel()
) {
    Scaffold(
        floatingActionButton = {
            ListButton(onClick = {
                imagesViewModel.addImage()
            })
        }
    ) {
        ImageList(
            list = imagesViewModel.images,
            imageDownloader
        ) { i: Int, downloaded: ImageDownloaderResult ->
            imagesViewModel.setDownloaded(i, downloaded)
        }
    }
}

@Composable
fun ListButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Text("Add an image", modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun ImageList(
    list: List<DownloadedImage>,
    imageDownloader: Supplier<ImageDownloader>,
    onImageDownloaded: (index: Int, result: ImageDownloaderResult) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(
            items = list,
            itemContent = { index, item ->
                ImageListItem(item) {
                    val download = imageDownloader.get().downloadImage(item.url)
                    onImageDownloaded(index, download)
                }
            })
    }
}

@Composable
fun ImageListItem(image: DownloadedImage, triggerImageDownload: suspend () -> Unit) {
    Row(
        Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val downloaderResult = image.downloaderResult
        when {
            // Download pending / in progress
            downloaderResult == null -> {
                PendingDownloadImageListItem(image.url)

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        triggerImageDownload()
                    }
                }
            }
            // Download successful
            downloaderResult.successful -> {
                DownloadedImageListItem(downloaderResult)
            }
            // Download unsuccessful
            else -> {
                Text("Something went horribly wrong, see logcat for details.")
            }
        }
    }
}

@Composable
fun RowScope.PendingDownloadImageListItem(url: String) {
    Column {
        CircularProgressIndicator()
    }
    Column {
        Text("Downloading $url...")
    }
}

@Composable
fun RowScope.DownloadedImageListItem(download: ImageDownloaderResult) {
    Column {
        Image(
            BitmapFactory.decodeByteArray(
                download.blob, 0, download.blob.size
            ).asImageBitmap(),
            null,
            Modifier
                .clip(RoundedCornerShape(20.dp))
        )
    }
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Latency: ${download.latency.toMillis()}ms")
        Text("Cached: ${download.wasCached}")
        Text("Downloaded by: ${download.downloaderRef}")
    }
}