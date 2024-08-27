package dev.borisochieng.artio

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.database
import dev.borisochieng.artio.di.AppModule.appModule
import dev.borisochieng.artio.di.PersistenceModule.persistenceModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ArtioApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Firebase.database.setPersistenceEnabled(true)

        startKoin {
            androidContext(this@ArtioApp)
            androidLogger(Level.INFO)
            modules(appModule, persistenceModule)
        }
    }
}