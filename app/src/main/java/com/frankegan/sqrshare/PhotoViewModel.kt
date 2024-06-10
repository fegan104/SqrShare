package com.frankegan.sqrshare

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoViewModel(private val app: Application): AndroidViewModel(app) {
    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    private val _selectedImageColor = MutableStateFlow<Color?>(null)
    private val _shareImageEvent = MutableSharedFlow<Uri?>()

    /**
     * The square bitmap of the selected image.
     */
    val selectedImageBitmap: Flow<Bitmap?> = _selectedImageBitmap

    /**
     * A vibrant color picked from the selected image.
     */
    val selectedImageColor: Flow<Color?> = _selectedImageColor

    /**
     * The Uri for a square has been saved and should now be shared.
     */
    val shareImageEvents: Flow<Uri> = _shareImageEvent.filterNotNull()

    /**
     * An image has been selected either picked form the gallery or as an intent
     * extra. The file will be loaded as a square bitmap with white borders and a
     * vibrant color extracted from the image. The bitmap and extracted color will
     * be emitted by [selectedImageBitmap] and [selectedImageColor] respectively.
     */
    fun onImageSelected(imageUri: Uri) = viewModelScope.launch {
        val square = SqrBitmapGenerator.generate(app, imageUri)
        _selectedImageBitmap.value = square
        _selectedImageColor.value = extractVibrantColor(square)
    }

    /**
     * Saves the requested image then emits the file uri to the
     * [shareImageEvents] Flow. If the file cannot be saved no uri will be emitted.
     *
     * @param image The bitmap to be saved.
     * @param rotationDegrees The number of degrees to rotate the bitmap before saving.
     */
    fun onShareImage(image: Bitmap, rotationDegrees: Float) = viewModelScope.launch {
        _shareImageEvent.emit(saveImage(image, rotationDegrees))
    }

    /**
     * Pick a vibrant color from the image.
     *
     * @param bitmap The image to extract colors from.
     * @return A vibrant [Color] or null if none can be found.
     */
    private suspend fun extractVibrantColor(bitmap: Bitmap?) = withContext(Dispatchers.Default) {
        bitmap ?: return@withContext null
        Palette.from(bitmap).generate().vibrantSwatch?.rgb?.let { colorInt ->
            Color(colorInt)
        }
    }

    /**
     * Saves the given bitmap to the app-specific external directory.
     *
     * @param image The original bitmap that was loaded. The image displayed in
     * the UI will have the rotation applied visually but the bitmap will not be
     * rotated until it is written to the output file.
     * @param rotationDegrees The number of degrees to rotate the original image before saving.
     * @return The uri of the new file or null if it was unsuccessful.
     */
    private suspend fun saveImage(image: Bitmap, rotationDegrees: Float): Uri? = withContext(Dispatchers.IO) {
        val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val outputFile = File(app.getExternalFilesDir(null), "SQR_$formattedTimestamp.png")
        val matrix = Matrix().apply {
            postRotate(rotationDegrees)
        }
        val rotatedBitmap = Bitmap.createBitmap(
            image, 0, 0,
            image.width,
            image.height,
            matrix, true,
        )
        try {
            FileOutputStream(outputFile).use {
                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (fnf: FileNotFoundException) {
            Log.e("MainActivity", "Couldn't save file", fnf)
        }

        FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", outputFile)
    }
}