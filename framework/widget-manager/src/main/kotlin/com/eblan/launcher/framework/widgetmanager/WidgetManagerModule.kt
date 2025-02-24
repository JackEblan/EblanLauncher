package com.eblan.launcher.framework.widgetmanager

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface WidgetManagerModule {

    @Binds
    @Singleton
    fun appWidgetManagerWrapper(impl: AndroidAppWidgetManagerWrapper): AppWidgetManagerWrapper
}