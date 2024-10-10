package dev.borisochieng.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [dev.borisochieng.model.Sketch::class], version = 1)
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

	abstract fun sketchDao(): SketchDao

}