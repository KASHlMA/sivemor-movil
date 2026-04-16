package com.sivemore.mobile.feature.inspection

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

internal fun createPersistentCaptureUri(context: Context): Uri {
    val imageDir = File(context.filesDir, "captured-evidence").apply { mkdirs() }
    val imageFile = File.createTempFile("capture_", ".jpg", imageDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile,
    )
}
