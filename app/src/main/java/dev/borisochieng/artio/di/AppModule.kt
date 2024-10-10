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
        viewModel { AuthViewModel() }
        viewModel { HomeViewModel() }
        viewModel { SketchPadViewModel() }
    }
}