package dev.borisochieng.sketchpad.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import dev.borisochieng.sketchpad.database.AppDatabase
import dev.borisochieng.sketchpad.database.SketchDao
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import dev.borisochieng.sketchpad.database.repository.SketchRepositoryImpl
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.KeyValueStore
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.dataStore
import org.koin.dsl.module

object PersistenceModule {

	private fun provideDatabase(context: Context): AppDatabase {
		return Room.databaseBuilder(
			context = context,
			klass = AppDatabase::class.java,
			name = "sketch-app-database"
		).build()
	}

	private fun provideSketchDao(appDatabase: AppDatabase): SketchDao {
		return appDatabase.sketchDao()
	}

	private fun provideDatastore(context: Context): DataStore<Preferences> = context.dataStore

	val persistenceModule = module {
		single { provideDatabase(get<Context>().applicationContext) }
		single { provideDatastore(get<Context>().applicationContext) }
		single { provideSketchDao(get()) }
		factory { KeyValueStore() }
		factory<SketchRepository> { SketchRepositoryImpl() }
	}

}