package dev.borisochieng.datastore_preferences.repository

import android.Manifest
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val PERMISSION_CODE = 100

class PreferenceDataStoreRepository : KoinComponent {

    private val dataStore by inject<DataStore<Preferences>>()

    // Preference Keys
    val themePreferenceKey = intPreferencesKey("list_theme")
    val firstLaunchKey = booleanPreferencesKey("first_launch")

    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

// Retrieving functions
    /**
     * extension [isDarkThemeOn] checks the saved theme from preference
     * and returns boolean
     */
    fun Context.isDarkThemeOn() = dataStore.data
        .map { preferences ->
            // No type safety.
            preferences[themePreferenceKey] ?: 0
        }

//fun Color.convertToOldColor(): Int {
//    val color = this.toArgb()
//    return android.graphics.Color.argb(
//        color.alpha,
//        color.red,
//        color.green,
//        color.blue
//    )
//}

    fun getLaunchStatus(): Flow<Boolean> {
        return dataStore.data.map {
            it[firstLaunchKey] ?: false
        }
    }

    suspend fun saveLaunchStatus() {
        dataStore.edit {
            it[firstLaunchKey] = true
        }
    }
}