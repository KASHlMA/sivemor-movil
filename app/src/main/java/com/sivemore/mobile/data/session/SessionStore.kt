package com.sivemore.mobile.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sivemore.mobile.domain.model.AuthenticatedUser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences: SharedPreferences = createPreferences(context)

    fun hasActiveSession(): Boolean = accessToken() != null && refreshToken() != null

    fun accessToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    fun refreshToken(): String? = preferences.getString(KEY_REFRESH_TOKEN, null)

    fun currentUser(): AuthenticatedUser? {
        val id = preferences.getLong(KEY_USER_ID, -1L)
        if (id <= 0L) return null
        val username = preferences.getString(KEY_USERNAME, null) ?: return null
        val fullName = preferences.getString(KEY_FULL_NAME, null) ?: return null
        val role = preferences.getString(KEY_ROLE, null) ?: return null
        return AuthenticatedUser(id = id, username = username, fullName = fullName, role = role)
    }

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        user: AuthenticatedUser,
    ) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_FULL_NAME, user.fullName)
            .putString(KEY_ROLE, user.role)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun createPreferences(context: Context): SharedPreferences {
        return runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFERENCES_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }.getOrElse {
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }

    private companion object {
        private const val PREFERENCES_NAME = "sivemore_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLE = "role"
    }
}
