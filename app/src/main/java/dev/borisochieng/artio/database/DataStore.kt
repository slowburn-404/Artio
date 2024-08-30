package dev.borisochieng.artio.database

import android.Manifest
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// DataStore Instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Preference Keys
val themePreferenceKey = intPreferencesKey("list_theme")
val firstLaunchKey = booleanPreferencesKey("first_launch")

val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
const val PERMISSION_CODE = 100

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

fun Color.convertToOldColor(): Int {
    val color = this.toArgb()
    return android.graphics.Color.argb(
        color.alpha,
        color.red,
        color.green,
        color.blue
    )
}


class KeyValueStore : KoinComponent {
    private val dataStore by inject<DataStore<Preferences>>()

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