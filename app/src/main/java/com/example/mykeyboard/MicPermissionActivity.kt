package com.example.mykeyboard

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle

/** شفّافة: تطلب صلاحية المايكروفون مرة واحدة ثم تُغلق (الكيبورد نفسه لا يستطيع طلبها) */
class MicPermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        finish()
    }
}
