package com.example.signinregister.di

import android.content.Context
import com.example.signinregister.R
import com.example.signinregister.data.LocationTracker
import com.example.signinregister.data.LocationTrackerImpl
import com.example.signinregister.data.repository.AuthRepository
import com.example.signinregister.data.datastore.UserPreferencesManager
import com.example.signinregister.data.remote.VehicleService
import com.example.signinregister.data.repository.VehicleRepository
import com.example.signinregister.domain.repository.PlacesRepository
import com.example.signinregister.domain.repository.PlacesRepositoryImpl
import com.example.signinregister.domain.repository.RouteRepository
import com.example.signinregister.domain.repository.RouteRepositoryImpl
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- FIREBASE / AUTH ---

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserPreferencesManager(@ApplicationContext context: Context): UserPreferencesManager =
        UserPreferencesManager(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
        preferencesManager: UserPreferencesManager,
        googleSignInClient: GoogleSignInClient


    ): AuthRepository = AuthRepository(auth, db, preferencesManager, googleSignInClient)


    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // --- GOOGLE LOCATION SERVICES ---

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    // --- PLACES API  ---

    @Provides
    @Singleton
    fun providePlacesRepository(
        @ApplicationContext context: Context
    ): PlacesRepository {
        return PlacesRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): retrofit2.Retrofit {
        return retrofit2.Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideVehicleService(retrofit: Retrofit): VehicleService {
        return retrofit.create(VehicleService::class.java)
    }

    @Provides
    @Singleton
    fun provideVehicleRepository(service: VehicleService): VehicleRepository {
        return VehicleRepository(service)
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationBindsModule {


    @Binds
    @Singleton
    abstract fun bindLocationTracker(
        locationTrackerImpl: LocationTrackerImpl
    ): LocationTracker

    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        routeRepositoryImpl: RouteRepositoryImpl
    ): RouteRepository

}