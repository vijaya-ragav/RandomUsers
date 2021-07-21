package com.ragavan.randomusers.di

import android.app.Application
import com.ragavan.randomusers.database.RandomUserDao
import com.ragavan.randomusers.database.RemoteKeysDao
import com.ragavan.randomusers.database.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DepedencyModule {

    @Singleton
    @Provides
    fun getRandomUserDao(userDatabase: UserDatabase):RandomUserDao{
        return userDatabase.getUserDao()
    }
    @Singleton
    @Provides
    fun getRemoteKeysDao(userDatabase: UserDatabase): RemoteKeysDao {
        return userDatabase.remoteKeysDao()
    }

    @Singleton
    @Provides
    fun getUserDatabase(context: Application):UserDatabase{
        return UserDatabase.getInstance(context)
    }

}