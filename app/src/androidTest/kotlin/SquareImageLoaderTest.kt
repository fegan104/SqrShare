import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.frankegan.sqrshare.SqrBitmapGenerator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.test.assertTrue


@RunWith(AndroidJUnit4::class)
class SquareImageLoaderTest {

    @Test
    fun testLoadFromUri() = runTest {
        // Copy file from assets
        val context: Context = ApplicationProvider.getApplicationContext()
        val assetManager: AssetManager = InstrumentationRegistry.getInstrumentation().context.assets
        val inputStream = assetManager.open("testimage.jpg")

        //write to app-specific external files dir
        val outputDir = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
        val outputFile = File(outputDir, "testimage.jpg")
        val outputStream: OutputStream = FileOutputStream(outputFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.close()

        val imageUri = FileProvider.getUriForFile(
            InstrumentationRegistry.getInstrumentation().targetContext,
            InstrumentationRegistry.getInstrumentation().targetContext.packageName + ".fileprovider",
            outputFile
        )

        // Generate square bitmap
        val bitmap: Bitmap? = SqrBitmapGenerator.generate(context, imageUri)

        // Assert
        assertNotNull("Bitmap should not be null", bitmap)
        assertTrue { bitmap!!.width == bitmap.height }
    }
}