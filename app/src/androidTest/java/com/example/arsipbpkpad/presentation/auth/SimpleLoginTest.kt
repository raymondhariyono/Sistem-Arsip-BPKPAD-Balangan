package com.example.arsipbpkpad.presentation.auth

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SimpleLoginTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun simpleTest() {
        composeTestRule.setContent {
            Text("Hello Test")
        }
        composeTestRule.onNodeWithText("Hello Test").assertExists()
    }
}
