package com.eblan.launcher.framework.widgetmanager

import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
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
    fun androidAppWidgetManagerWrapper(impl: DefaultAppWidgetManagerWrapper): AndroidAppWidgetManagerWrapper

    @Binds
    @Singleton
    fun appWidgetManagerWrapper(impl: DefaultAppWidgetManagerWrapper): AppWidgetManagerWrapper

    @Binds
    @Singleton
    fun androidAppWidgetHostWrapper(impl: DefaultAppWidgetHostWrapper): AndroidAppWidgetHostWrapper

    @Binds
    @Singleton
    fun appWidgetHostWrapper(impl: DefaultAppWidgetHostWrapper): AppWidgetHostWrapper
}