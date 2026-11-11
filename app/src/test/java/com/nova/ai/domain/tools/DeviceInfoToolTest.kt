package com.nova.ai.domain.tools

import android.content.Context
import android.os.BatteryManager
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DeviceInfoToolTest {

    private lateinit var mockContext: Context
    private lateinit var mockBatteryManager: BatteryManager
    private lateinit var tool: DeviceInfoTool

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockBatteryManager = mock(BatteryManager::class.java)
        `when`(mockContext.getSystemService(Context.BATTERY_SERVICE)).thenReturn(mockBatteryManager)
        `when`(mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).thenReturn(85)
        
        // Note: StatFs uses direct native file paths; for full testing Robolectric or Mockito static can be utilized.
    }

    @Test
    fun toolName_isCorrect() {
        tool = DeviceInfoTool(mockContext)
        assertTrue(tool.name == "DeviceInfo")
        assertNotNull(tool.description)
    }
}
