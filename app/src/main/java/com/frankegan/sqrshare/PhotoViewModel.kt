package com.frankegan.sqrshare

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class PhotoViewModel(private val app: Application): AndroidViewModel(app) {
    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    private val _selectedImageColor = MutableStateFlow<Color?>(null)

    val selectedImageBitmap: Flow<Bitmap?> = _selectedImageBitmap

    val selectedImageColor: Flow<Color?> = _selectedImageColor

    fun onImageSelected(imageUri: Uri) = viewModelScope.launch {
        val bitmap = SqrBitmapGenerator.generate(app, imageUri)
        _selectedImageBitmap.value = bitmap
        _selectedImageColor.value = Palette.from(bitmap).generate().vibrantSwatch?.rgb?.let { colorInt ->
            Color(colorInt)
        }
    }

    fun saveImage(image: Bitmap, rotation: Float) {

    }
}