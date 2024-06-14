package com.frankegan.sqrshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.frankegan.sqrshare.ui.SqrShareTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<PhotoViewModel>()

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMedia = registerForActivityResult(PickVisualMedia()) { imageUri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (imageUri != null) {
            viewModel.onImageSelected(imageUri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.action
        val type = intent.type
        //this if for apps sharing to the app
        if (Intent.ACTION_SEND == action && type != null && type.startsWith("image/")) {
            handleSentImage(intent)
        }
        //this is for apps trying to open images with our app
        if (Intent.ACTION_VIEW == action && type != null && type.startsWith("image/")) {
            handleViewImage(intent)
        }

        //Collect events to be notified when an image file is ready to be shared
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shareImageEvents.collect { savedImage ->
                    shareImage(savedImage)
                }
            }
        }

        setContent {
            val imageBitmap by viewModel.selectedImageBitmap.collectAsStateWithLifecycle(null)
            val selectedColor by viewModel.selectedImageColor.collectAsStateWithLifecycle(null)

            SqrShareTheme(selectedColor) {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    PictureScreen(
                        image = imageBitmap,
                        onOpenGallery = {
                            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        },
                        onShare = { image, rotationDegrees ->
                            viewModel.onShareImage(image, rotationDegrees)
                        }
                    )
                }
            }
        }
    }

    /**
     * A helper method for when an app shares an [android.content.Intent] to be opened by our app.
     *
     * @param intent The [android.content.Intent] of the image to be displayed.
     */
    private fun handleSentImage(intent: Intent) {
        val imageUri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java) ?: return
        viewModel.onImageSelected(imageUri)
    }

    /**
     * A helper method for when an app shares an [android.content.Intent] to be Viewed in our app.
     *
     * @param intent The [android.content.Intent] of the image to be displayed.
     */
    private fun handleViewImage(intent: Intent) {
        val imageUri = intent.data ?: return
        viewModel.onImageSelected(imageUri)
    }

    /**
     * Sends the given image uri to the system share sheet, or nothing if the uri is null.
     */
    private fun shareImage(savedImage: Uri?) {
        savedImage ?: return
        val shareIntent = Intent()
            .setAction(Intent.ACTION_SEND)
            .setType("image/*")
            .putExtra(Intent.EXTRA_STREAM, savedImage)
        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
    }
}