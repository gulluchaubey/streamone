package com.learnapp.livestream.di

import com.google.gson.Gson
import com.learnapp.livestream.data.network.remote.IRemoteOperations
import com.learnapp.livestream.data.network.remote.RemoteDataSource
import com.learnapp.livestream.data.network.remote.RemoteOperations
import com.learnapp.livestream.data.network.remote.api.CatalogApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun provideCatalogApi(
        remoteDataSource: RemoteDataSource
    ): CatalogApi {
        return remoteDataSource.buildApi(
            CatalogApi::class.java
        )
    }

    @Singleton
    @Provides
    fun provideRemoteApiCall(remoteOperations: RemoteOperations): IRemoteOperations {
        return remoteOperations
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }
}
