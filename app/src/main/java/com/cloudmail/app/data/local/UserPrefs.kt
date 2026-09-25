package com.cloudmail.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "cloud_mail_prefs")

/**
 * 普通偏好：最近使用的账号、语言等（token 见 TokenStore）。
 */
@Singleton
class UserPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val lastAccountId: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_ACCOUNT] ?: 0L }

    val language: Flow<String> = context.dataStore.data.map { it[KEY_LANG] ?: "zh" }

    suspend fun setLastAccountId(accountId: Long) {
        context.dataStore.edit { it[KEY_LAST_ACCOUNT] = accountId }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANG] = lang }
    }

    private companion object {
        val KEY_LAST_ACCOUNT = longPreferencesKey("last_account_id")
        val KEY_LANG = stringPreferencesKey("language")
    }
}
