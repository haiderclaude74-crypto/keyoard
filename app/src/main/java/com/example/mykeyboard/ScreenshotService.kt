package com.example.mykeyboard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/** خدمة إمكانية وصول خفيفة، وظيفتها الوحيدة تنفيذ "لقطة الشاشة" عند ضغط زر الكيبورد */
class ScreenshotService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance === this) instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        var instance: ScreenshotService? = null

        fun shoot(): Boolean {
            val s = instance ?: return false
            return s.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        }
    }
}
