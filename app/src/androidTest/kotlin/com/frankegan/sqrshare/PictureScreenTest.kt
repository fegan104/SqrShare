package com.frankegan.sqrshare

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frankegan.sqrshare.ui.SqrShareTheme
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PictureScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun openGallery() {
        var galleryOpened = false
        composeTestRule.activity.setContent {
            SqrShareTheme(computedColor = null) {
                PictureScreen(
                    image = null,
                    onOpenGallery = {
                        galleryOpened = true
                    },
                    onShare = { _, _ -> },
                )
            }
        }

        assertFalse(galleryOpened)
        composeTestRule.onNodeWithContentDescription("gallery").performClick()
        assertTrue(galleryOpened)

        composeTestRule.onNodeWithText("Tap anywhere to get started").assertIsDisplayed()
    }

    @Test
    fun shareImage() = runTest {
        val imageUri = getUriForFile("testimage.jpg")
        val context: Context = ApplicationProvider.getApplicationContext()
        val bitmap = SqrBitmapGenerator.generate(context, imageUri)
        var shared = false
        // Start the app
        composeTestRule.activity.setContent {
            SqrShareTheme(computedColor = null) {
                PictureScreen(
                    image = bitmap,
                    onOpenGallery = {},
                    onShare = { _, _ ->
                        shared = true
                    },
                )
            }
        }

        assertFalse(shared)
        with(composeTestRule.onNodeWithContentDescription("share")) {
            assertIsDisplayed()
            performClick()
        }
        assertTrue(shared)

        composeTestRule.onNodeWithText("Tap anywhere to get started").assertDoesNotExist()
    }

    @Test
    fun rotateImage() = runTest {
        val imageUri = getUriForFile("testimage.jpg")
        val context: Context = ApplicationProvider.getApplicationContext()
        val bitmap = SqrBitmapGenerator.generate(context, imageUri)
        var rotationDegrees = 0f
        // Start the app
        composeTestRule.activity.setContent {
            SqrShareTheme(computedColor = null) {
                PictureScreen(
                    image = bitmap,
                    onOpenGallery = {},
                    onShare = { _, rotation ->
                        rotationDegrees = rotation
                    },
                )
            }
        }

        assertEquals(0f, rotationDegrees)
        val rotateButton = composeTestRule.onNodeWithContentDescription("rotate left")
        val shareButton = composeTestRule.onNodeWithContentDescription("share")

        with(rotateButton) {
            assertIsDisplayed()
            performClick()
        }
        with(shareButton) {
            assertIsDisplayed()
            performClick()
        }
        assertEquals(-90f, rotationDegrees)
        rotateButton.performClick()
        shareButton.performClick()
        assertEquals(-180f, rotationDegrees)

        composeTestRule.onNodeWithText("Tap anywhere to get started").assertDoesNotExist()
    }
}