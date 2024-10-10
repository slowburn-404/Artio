package dev.borisochieng.artio.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import dev.borisochieng.database.database.AppDatabase
import dev.borisochieng.database.database.KeyValueStore
import dev.borisochieng.database.database.SketchDao
import dev.borisochieng.database.database.dataStore
import dev.borisochieng.database.database.repository.SketchRepository
import dev.borisochieng.database.database.repository.SketchRepositoryImpl
import org.koin.dsl.module

object PersistenceModule {

	private fun provideDatabase(context: Context): dev.borisochieng.database.database.AppDatabase {
		return Room.databaseBuilder(
			context = context,
			klass = dev.borisochieng.database.database.AppDatabase::class.java,
			name = "sketch-app-database"
		).build()
	}

	private fun provideSketchDao(appDatabase: dev.borisochieng.database.database.AppDatabase): dev.borisochieng.database.database.SketchDao {
		return appDatabase.sketchDao()
	}

	val persistenceModule = module {
		single { provideDatabase(get<Context>().applicationContext) }
		single { provideSketchDao(get()) }
		factory<dev.borisochieng.database.database.repository.SketchRepository> { dev.borisochieng.database.database.repository.SketchRepositoryImpl() }
	}

}