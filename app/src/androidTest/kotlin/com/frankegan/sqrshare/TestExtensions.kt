package com.frankegan.sqrshare

import android.content.res.AssetManager
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun getUriForFile(fileName: String): Uri {
    val assetManager: AssetManager = InstrumentationRegistry.getInstrumentation().context.assets
    val inputStream = assetManager.open(fileName)

    //write to app-specific external files dir
    val outputDir = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
    val outputFile = File(outputDir, fileName)
    val outputStream: OutputStream = FileOutputStream(outputFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) {
        outputStream.write(buffer, 0, length)
    }

    inputStream.close()
    outputStream.close()

    return FileProvider.getUriForFile(
        InstrumentationRegistry.getInstrumentation().targetContext,
        InstrumentationRegistry.getInstrumentation().targetContext.packageName + ".fileprovider",
        outputFile
    )
}