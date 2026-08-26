package com.akashboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AkashBoardIMETest {
    @Test
    fun testLifecycle() {
        val controller = Robolectric.buildService(AkashBoardIME::class.java)
        controller.create() // calls onCreate()
        val ime = controller.get()
        assertNotNull(ime)
        controller.destroy()
    }
}
