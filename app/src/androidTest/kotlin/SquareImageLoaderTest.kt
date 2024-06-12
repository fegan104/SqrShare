import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frankegan.sqrshare.SqrBitmapGenerator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue


@RunWith(AndroidJUnit4::class)
class SquareImageLoaderTest {

    @Test
    fun testLoadFromUri() = runTest {
        // Copy file from assets
        val context: Context = ApplicationProvider.getApplicationContext()
        val imageUri = getUriForFile("testimage.jpg")

        // Generate square bitmap
        val bitmap: Bitmap? = SqrBitmapGenerator.generate(context, imageUri)

        // Assert
        assertNotNull("Bitmap should not be null", bitmap)
        assertTrue { bitmap!!.width == bitmap.height }
    }
}