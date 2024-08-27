package dev.borisochieng.artio.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dev.borisochieng.artio.auth.data.AuthRepositoryImpl
import dev.borisochieng.artio.auth.domain.AuthRepository
import dev.borisochieng.artio.collab.data.CollabRepositoryImpl
import dev.borisochieng.artio.collab.domain.CollabRepository
import dev.borisochieng.artio.ui.screens.auth.AuthViewModel
import dev.borisochieng.artio.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.artio.ui.screens.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val appModule = module {
        factory<AuthRepository> { AuthRepositoryImpl() }
        single<FirebaseUser?> { FirebaseAuth.getInstance().currentUser }
        single<FirebaseDatabase> {FirebaseDatabase.getInstance()}
        factory<CollabRepository> { CollabRepositoryImpl(get()) }
        viewModel { AuthViewModel() }
        viewModel { HomeViewModel() }
        viewModel { SketchPadViewModel() }
    }
}