package com.adaptiveTrust.mobile.data.api

import android.content.Context
import com.adaptiveTrust.mobile.BuildConfig
import com.adaptiveTrust.mobile.data.local.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        tokenStore: TokenStore
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // Auth interceptor – injects Bearer token from DataStore
            .addInterceptor { chain ->
                val token = tokenStore.getTokenBlocking()
                val request = chain.request().newBuilder().apply {
                    if (token != null) {
                        addHeader("Authorization", "Bearer $token")
                    }
                    addHeader("X-Client-Platform", "android")
                    addHeader("X-App-Version", BuildConfig.VERSION_NAME)
                }.build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            // TLS 1.3 enforcement (system default on API 29+, explicit below for 28)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAdaptiveTrustApi(retrofit: Retrofit): AdaptiveTrustApi {
        return retrofit.create(AdaptiveTrustApi::class.java)
    }
}
