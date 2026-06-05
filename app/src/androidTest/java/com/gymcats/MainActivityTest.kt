package com.gymcats

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesWithoutCrashing() {
        composeRule.waitForIdle()

        assertNotNull(composeRule.activity)
        assertEquals("com.gymcats", composeRule.activity.packageName)
        composeRule.onRoot()
    }
}
