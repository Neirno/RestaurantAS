package com.neirno.restaurantas.di

import OrdersRepositoryImpl
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.neirno.restaurantas.data.repository.AuthRepositoryImpl
import com.neirno.restaurantas.data.repository.TablesRepositoryImpl
import com.neirno.restaurantas.data.repository.UserRepositoryImpl
import com.neirno.restaurantas.domain.repository.AuthRepository
import com.neirno.restaurantas.domain.repository.OrdersRepository
import com.neirno.restaurantas.domain.repository.TablesRepository
import com.neirno.restaurantas.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideTablesRepository(
        firestore: FirebaseFirestore
    ): TablesRepository {
        return TablesRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository {
        return UserRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideOrdersRepository(
        firestore: FirebaseFirestore
    ): OrdersRepository {
        return OrdersRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false) // Отключаем кэширование
            .build()
        val firestoreInstanse = FirebaseFirestore.getInstance()
        firestoreInstanse.firestoreSettings = firestoreSettings
        return firestoreInstanse
    }

}