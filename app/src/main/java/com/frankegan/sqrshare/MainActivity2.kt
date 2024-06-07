package com.frankegan.sqrshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frankegan.sqrshare.ui.SqrShareTheme

class MainActivity2 : AppCompatActivity() {
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
        setContent {
            val imageBitmap by viewModel.selectedImageBitmap.collectAsStateWithLifecycle(null)
            val selectedColor by viewModel.selectedImageColor.collectAsStateWithLifecycle(colorResource(R.color.app_primary))

            SqrShareTheme(selectedColor) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PictureScreen(
                        image = imageBitmap,
                        onOpenGallery = {
                            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        },
                        onShare = { image, rotationDegrees ->
                            viewModel.saveImage(image, rotation = rotationDegrees)
                        }
                    )
                }
            }
        }
    }
}