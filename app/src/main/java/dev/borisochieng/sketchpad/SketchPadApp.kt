package dev.borisochieng.sketchpad

import android.app.Application
import dev.borisochieng.sketchpad.di.AppModule.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SketchPadApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SketchPadApp)
            androidLogger(Level.INFO)
            modules(appModule)
        }
    }
}