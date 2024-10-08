package dev.borisochieng.datastore_preferences.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.borisochieng.datastore_preferences.repository.PreferenceDataStoreRepository
import org.koin.dsl.module

object DataStoreModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private fun providesPreferenceDatastore(context: Context): DataStore<Preferences> =
        context.dataStore

    internal val dataStoreModule = module {
        single { providesPreferenceDatastore(get<Context>().applicationContext) }
        factory<PreferenceDataStoreRepository> { PreferenceDataStoreRepository() }
    }
}