package com.eblan.launcher.broadcastreceiver

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PackageManagerModule {

    @Provides
    @Singleton
    fun packageBroadcastReceiver(): PackageBroadcastReceiver = PackageBroadcastReceiver()
}