package com.eblan.launcher.framework.launcherapps

import com.eblan.launcher.domain.framework.LauncherAppsWrapper
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
    fun launcherAppsWrapper(impl: DefaultLauncherAppsWrapper): LauncherAppsWrapper

    @Binds
    @Singleton
    fun androidLauncherAppsWrapper(impl: DefaultLauncherAppsWrapper): AndroidLauncherAppsWrapper

    @Binds
    @Singleton
    fun pinItemRequestWrapper(impl: DefaultPinItemRequestWrapper): PinItemRequestWrapper
}