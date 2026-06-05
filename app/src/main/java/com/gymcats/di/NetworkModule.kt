package com.gymcats.di

import com.gymcats.BuildConfig
import com.gymcats.data.remote.api.GymCatsApi
import com.gymcats.data.remote.auth.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = BuildConfig.API_BASE_URL

    @Provides
    @Singleton
    @Named("base")
    fun provideBaseOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(
        @Named("base") baseClient: OkHttpClient,
        authInterceptor: AuthInterceptor
    ): OkHttpClient = baseClient.newBuilder()
        .addInterceptor(authInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(@Named("auth") client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideGymCatsApi(retrofit: Retrofit): GymCatsApi =
        retrofit.create(GymCatsApi::class.java)
}
