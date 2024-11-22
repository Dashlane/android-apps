package com.dashlane.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.Until.newWindow
import com.dashlane.baselineprofile.BaselineTestHelper.CONTINUE_BUTTON_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.CREATE_MP_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.DEFAULT_TIMEOUT
import com.dashlane.baselineprofile.BaselineTestHelper.EMAIL_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.GET_STARTED_BUTTON_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.I_AGREE_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.LOGIN_BUTTON_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.NEVER_USED_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.PACKAGE_NAME
import com.dashlane.baselineprofile.BaselineTestHelper.SKIP_BUTTON_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.TEST_PW
import com.dashlane.baselineprofile.BaselineTestHelper.TYPE_MP_TEXT
import com.dashlane.baselineprofile.BaselineTestHelper.generateTestAccount
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = false,
            maxIterations = 1,
            stableIterations = 1
        ) {
            
            pressHome()
            startActivityAndWait()
            
            val login = device.findObject(By.text(LOGIN_BUTTON_TEXT))
            login.clickAndWait(newWindow(), DEFAULT_TIMEOUT)
            
            device.pressBack()
            
            device.pressBack()
            createAccount()
            skipOnboarding()
        }
    }

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            
            includeInStartupProfile = true,
            maxIterations = 5
        ) {
            
            

            
            pressHome()
            startActivityAndWait()

            
            
            
            
            

            
            

            
            val login = device.findObject(By.text(LOGIN_BUTTON_TEXT))
            login.clickAndWait(newWindow(), DEFAULT_TIMEOUT)
            
            device.pressBack()
            
            device.pressBack()
            
            val getStarted = device.findObject(By.text(GET_STARTED_BUTTON_TEXT))
            getStarted.clickAndWait(newWindow(), DEFAULT_TIMEOUT)
            device.pressBack()
        }
    }

    private fun MacrobenchmarkScope.createAccount() {
        
        device.findObject(By.text(GET_STARTED_BUTTON_TEXT))
            .clickAndWait(newWindow(), DEFAULT_TIMEOUT)
        
        device.findObject(By.text(EMAIL_TEXT)).text = generateTestAccount()
        device.findObject(By.text(CONTINUE_BUTTON_TEXT)).click()
        
        device.wait(Until.findObject(By.text(CREATE_MP_TEXT)), DEFAULT_TIMEOUT)
        device.findObject(By.text(CREATE_MP_TEXT)).text = TEST_PW
        device.findObject(By.text(CONTINUE_BUTTON_TEXT)).click()
        
        device.wait(Until.findObject(By.text(TYPE_MP_TEXT)), DEFAULT_TIMEOUT)
        device.findObject(By.text(TYPE_MP_TEXT)).text = TEST_PW
        device.findObject(By.text(CONTINUE_BUTTON_TEXT)).click()
        
        device.wait(Until.findObjects(By.checkable(true)), DEFAULT_TIMEOUT)
        device.findObjects(By.checkable(true)).forEach { it.click() }
        device.findObject(By.text(I_AGREE_TEXT)).click()
    }

    private fun MacrobenchmarkScope.skipOnboarding() {
        
        device.wait(Until.findObject(By.text(NEVER_USED_TEXT)), DEFAULT_TIMEOUT)
        device.findObject(By.text(NEVER_USED_TEXT)).clickAndWait(newWindow(), DEFAULT_TIMEOUT)
        
        device.wait(Until.findObject(By.text(SKIP_BUTTON_TEXT)), DEFAULT_TIMEOUT)
        device.findObject(By.text(SKIP_BUTTON_TEXT)).click()
        device.wait(Until.findObject(By.text(SKIP_BUTTON_TEXT)), DEFAULT_TIMEOUT)
        device.findObject(By.text(SKIP_BUTTON_TEXT)).click()
        device.wait(Until.findObject(By.text(SKIP_BUTTON_TEXT)), DEFAULT_TIMEOUT)
        device.findObject(By.text(SKIP_BUTTON_TEXT)).click()
        device.wait(Until.findObject(By.text(SKIP_BUTTON_TEXT)), DEFAULT_TIMEOUT)
        device.findObject(By.text(SKIP_BUTTON_TEXT)).click()
    }
}