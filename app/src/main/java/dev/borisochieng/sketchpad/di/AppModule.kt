package dev.borisochieng.sketchpad.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dev.borisochieng.sketchpad.auth.data.AuthRepositoryImpl
import dev.borisochieng.sketchpad.auth.domain.AuthRepository
import dev.borisochieng.sketchpad.auth.presentation.viewmodels.AuthViewModel
import dev.borisochieng.sketchpad.collab.data.CollabRepositoryImpl
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.sketchpad.ui.screens.home.HomeViewModel
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