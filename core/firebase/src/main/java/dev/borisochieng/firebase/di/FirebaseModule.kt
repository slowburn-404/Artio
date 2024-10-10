package dev.borisochieng.firebase.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dev.borisochieng.firebase.auth.FirebaseAuthDataSourceImpl
import dev.borisochieng.firebase.auth.FirebaseAuthDataSource
import dev.borisochieng.firebase.database.data.CollabRepositoryImpl
import dev.borisochieng.firebase.database.domain.CollabRepository
import org.koin.dsl.module

object FirebaseModule {

    internal val firebaseModule = module {

        single<FirebaseAuth> { FirebaseAuth.getInstance() }

        single<FirebaseDatabase> { FirebaseDatabase.getInstance() }

        factory<FirebaseAuthDataSource> { FirebaseAuthDataSourceImpl() }

        factory<CollabRepository> { CollabRepositoryImpl() }

    }
}