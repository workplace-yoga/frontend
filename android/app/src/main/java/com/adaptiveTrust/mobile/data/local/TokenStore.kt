package com.adaptiveTrust.mobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "adaptrust_prefs")

/**
 * Encrypted DataStore wrapper for the JWT access token.
 * In production: wrap with EncryptedSharedPreferences or Jetpack Security.
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val ROLE_KEY  = stringPreferencesKey("admin_role")
        private val USER_KEY  = stringPreferencesKey("admin_username")
    }

    suspend fun saveToken(token: String, role: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[ROLE_KEY]  = role
            prefs[USER_KEY]  = username
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }

    fun getTokenBlocking(): String? = runBlocking {
        context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun getRole(): String? =
        context.dataStore.data.map { it[ROLE_KEY] }.first()

    suspend fun getUsername(): String? =
        context.dataStore.data.map { it[USER_KEY] }.first()

    fun isLoggedInBlocking(): Boolean = getTokenBlocking() != null
}
