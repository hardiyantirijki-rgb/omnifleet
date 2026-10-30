package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PlatformType
import com.example.data.repository.DeviceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniFleet", appName)
    }

    @Test
    fun `repository loads multi-platform devices`() {
        val repository = DeviceRepository()
        val devices = repository.devices.value

        assertTrue("Devices should not be empty", devices.isNotEmpty())
        assertNotNull(devices.find { it.platform == PlatformType.IOS })
        assertNotNull(devices.find { it.platform == PlatformType.ANDROID })
        assertNotNull(devices.find { it.platform == PlatformType.WINDOWS })
        assertNotNull(devices.find { it.platform == PlatformType.LINUX })
    }
}
