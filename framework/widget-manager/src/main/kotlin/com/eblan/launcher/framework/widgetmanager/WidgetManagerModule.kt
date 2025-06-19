package com.eblan.launcher.framework.widgetmanager

import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
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
    fun appWidgetManagerController(impl: AndroidAppWidgetManagerWrapper): AppWidgetManagerController

    @Binds
    @Singleton
    fun appWidgetManagerWrapper(impl: AndroidAppWidgetManagerWrapper): AppWidgetManagerWrapper

    @Binds
    @Singleton
    fun appWidgetHostWrapper(impl: AndroidAppWidgetHostWrapper): AppWidgetHostWrapper
}