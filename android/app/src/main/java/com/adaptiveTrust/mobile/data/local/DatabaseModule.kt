package com.adaptiveTrust.mobile.data.local

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.buildDatabase(context)
    }

    @Provides
    fun provideUserCacheDao(db: AppDatabase): UserCacheDao = db.userCacheDao()

    @Provides
    fun provideAlertCacheDao(db: AppDatabase): AlertCacheDao = db.alertCacheDao()

    @Provides
    fun provideNudgeLogDao(db: AppDatabase): NudgeLogDao = db.nudgeLogDao()
}
