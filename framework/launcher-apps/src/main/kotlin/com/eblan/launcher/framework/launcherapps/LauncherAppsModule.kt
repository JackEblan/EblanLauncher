package com.eblan.launcher.framework.launcherapps

import com.eblan.launcher.domain.framework.LauncherAppsDomainWrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface LauncherAppsModule {

    @Binds
    @Singleton
    fun launcherAppsDomainWrapper(impl: AndroidLauncherAppsWrapper): LauncherAppsDomainWrapper

    @Binds
    @Singleton
    fun launcherAppsWrapper(impl: AndroidLauncherAppsWrapper): LauncherAppsWrapper
}