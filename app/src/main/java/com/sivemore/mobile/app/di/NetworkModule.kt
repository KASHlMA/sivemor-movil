package com.sivemore.mobile.app.di

import android.util.Log
import com.sivemore.mobile.BuildConfig
import com.sivemore.mobile.data.network.AuthApiService
import com.sivemore.mobile.data.network.AuthInterceptor
import com.sivemore.mobile.data.network.MobileApiService
import com.sivemore.mobile.data.network.TokenAuthenticator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideRequestTraceInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request()
        if (BuildConfig.DEBUG) {
            Log.d("NetworkTrace", "Request ${request.method} ${request.url}")
        }
        chain.proceed(request)
    }

    @Provides
    @Singleton
    @AuthClient
    fun provideAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        requestTraceInterceptor: Interceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(requestTraceInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    @BackendClient
    fun provideBackendOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
        requestTraceInterceptor: Interceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(requestTraceInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        moshi: Moshi,
        @AuthClient authOkHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(authOkHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    @BackendRetrofit
    fun provideBackendRetrofit(
        moshi: Moshi,
        @BackendClient backendOkHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(backendOkHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(
        @AuthRetrofit retrofit: Retrofit,
    ): AuthApiService {
        if (BuildConfig.DEBUG) {
            Log.d("NetworkModule", "AuthApiService baseUrl=${BuildConfig.API_BASE_URL}")
        }
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMobileApiService(
        @BackendRetrofit retrofit: Retrofit,
    ): MobileApiService = retrofit.create(MobileApiService::class.java)
}
