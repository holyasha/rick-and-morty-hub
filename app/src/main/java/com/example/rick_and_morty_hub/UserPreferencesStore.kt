package com.example.rick_and_morty_hub

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val showCharacterOrigin: Boolean = true
)

/** Два пользовательских параметра, которые лабораторная требует хранить в DataStore. */
object UserPreferencesStore {
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")
    private val showOriginKey = booleanPreferencesKey("show_character_origin")

    fun preferences(context: Context): Flow<UserPreferences> =
        context.applicationContext.userPreferencesDataStore.data.map { values ->
            UserPreferences(
                notificationsEnabled = values[notificationsKey] ?: true,
                showCharacterOrigin = values[showOriginKey] ?: true
            )
        }

    suspend fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        context.applicationContext.userPreferencesDataStore.edit { values ->
            values[notificationsKey] = enabled
        }
    }

    suspend fun setShowCharacterOrigin(context: Context, enabled: Boolean) {
        context.applicationContext.userPreferencesDataStore.edit { values ->
            values[showOriginKey] = enabled
        }
    }
}
