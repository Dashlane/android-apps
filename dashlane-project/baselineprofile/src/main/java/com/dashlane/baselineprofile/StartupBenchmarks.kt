package com.dashlane.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.CompilationMode.None
import androidx.benchmark.macro.CompilationMode.Partial
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.dashlane.baselineprofile.BaselineTestHelper.DEFAULT_TIMEOUT
import com.dashlane.baselineprofile.BaselineTestHelper.GET_STARTED_BUTTON_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.PACKAGE_NAME
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupCompilationNone() = benchmark(None())

    @Test
    fun startupCompilationBaselineProfiles() = benchmark(Partial(BaselineProfileMode.Require))

    private fun benchmark(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = {
                pressHome()
            },
            measureBlock = {
                startActivityAndWait()

                
                
                
                

                
                
                
                device.wait(Until.findObject(By.text(GET_STARTED_BUTTON_TEXT)), DEFAULT_TIMEOUT)
            }
        )
    }
}