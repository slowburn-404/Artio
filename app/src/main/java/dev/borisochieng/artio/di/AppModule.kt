package dev.borisochieng.artio.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dev.borisochieng.firebase.auth.data.AuthRepositoryImpl
import dev.borisochieng.firebase.auth.domain.AuthRepository
import dev.borisochieng.firebase.database.data.CollabRepositoryImpl
import dev.borisochieng.firebase.database.domain.CollabRepository
import dev.borisochieng.artio.ui.screens.auth.AuthViewModel
import dev.borisochieng.artio.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.artio.ui.screens.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val appModule = module {
        factory<dev.borisochieng.firebase.auth.domain.AuthRepository> { dev.borisochieng.firebase.auth.data.AuthRepositoryImpl() }
        single<FirebaseUser?> { FirebaseAuth.getInstance().currentUser }
        single<FirebaseDatabase> {FirebaseDatabase.getInstance()}
        factory<dev.borisochieng.firebase.database.domain.CollabRepository> {
            dev.borisochieng.firebase.database.data.CollabRepositoryImpl(
                get()
            )
        }
        viewModel { AuthViewModel() }
        viewModel { HomeViewModel() }
        viewModel { SketchPadViewModel() }
    }
}