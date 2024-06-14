package com.frankegan.sqrshare

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog

/**
 * @param image The square image to display or null if none has been selected yet.
 * @param onOpenGallery Requests the image picker be launched.
 * @param onShare A lambda function that is triggered when the "Share" button
 * is clicked. It takes the [Bitmap] and the current rotation degrees as parameters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureScreen(
    image: Bitmap?,
    onOpenGallery: () -> Unit,
    onShare: (bitmap: Bitmap, rotationDegrees: Float) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var rotation by rememberSaveable { mutableFloatStateOf(0f) }
    val animatedAngle by animateFloatAsState(
        label = "image_angle",
        targetValue = rotation,
        animationSpec = tween(durationMillis = 300),
    )

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("SqrShare", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    if (image != null) {
                        IconButton(onClick = { onShare(image, rotation) }) {
                            Icon(
                                painterResource(R.drawable.baseline_share_24),
                                contentDescription = "share",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        IconButton(onClick = { rotation -= 90f }) {
                            Icon(
                                painterResource(R.drawable.baseline_rotate_left_24),
                                contentDescription = "rotate left",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "more", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(onClick = { showAboutDialog = true }, text = { Text("About") })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenGallery, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(
                    painterResource(R.drawable.baseline_photo_library_24),
                    contentDescription = "gallery",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        content = { padding ->
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(padding)
                    .fillMaxSize()
                    .clickable(enabled = (image == null)) { onOpenGallery() },
            ) {
                if (image != null) {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "selected image",
                        modifier = Modifier.fillMaxSize().rotate(animatedAngle),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text(
                        "Tap anywhere to get started",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )

    if (showAboutDialog) {
        AboutDialog(onDismissRequest = { showAboutDialog = false })
    }
}

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismissRequest) {
                    Text("OK", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
