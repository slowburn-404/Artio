package dev.borisochieng.sketchpad.di

import dev.borisochieng.sketchpad.auth.data.SignUpRepositoryImpl
import dev.borisochieng.sketchpad.auth.domain.SignUpRepository
import dev.borisochieng.sketchpad.auth.presentation.SignUpViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val appModule = module {
        single<SignUpRepository> { SignUpRepositoryImpl() }
        viewModel { SignUpViewModel() }
    }
}