package com.sivemore.mobile.data.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.sivemore.mobile.domain.model.EvidenceUpload
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class MediaUploadResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun toMultipart(upload: EvidenceUpload): MultipartBody.Part {
        val uri = Uri.parse(upload.uri)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No fue posible abrir el archivo seleccionado.")
        val mimeType = upload.mimeType ?: context.contentResolver.getType(uri) ?: "application/octet-stream"
        val fileName = upload.fileName ?: resolveFileName(uri) ?: "evidence-${System.currentTimeMillis()}.jpg"
        return MultipartBody.Part.createFormData(
            "file",
            fileName,
            bytes.toRequestBody(mimeType.toMediaTypeOrNull()),
        )
    }
    private fun resolveFileName(uri: Uri): String? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return null
    }
}
