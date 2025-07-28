package com.eblan.launcher.framework.wallpapermanager

import com.eblan.launcher.domain.framework.WallpaperManagerDomainWrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface WallpaperManagerModule {

    @Binds
    @Singleton
    fun wallpaperManagerDomainWrapper(impl: AndroidWallpaperManagerWrapper): WallpaperManagerDomainWrapper

    @Binds
    @Singleton
    fun wallpaperManagerWrapper(impl: AndroidWallpaperManagerWrapper): WallpaperManagerWrapper
}