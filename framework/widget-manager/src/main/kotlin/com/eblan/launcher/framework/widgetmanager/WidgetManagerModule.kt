package com.eblan.launcher.framework.widgetmanager

import com.eblan.launcher.domain.framework.AppWidgetHostDomainWrapper
import com.eblan.launcher.domain.framework.AppWidgetManagerDomainWrapper
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

    @Binds
    @Singleton
    fun appWidgetManagerDomainWrapper(impl: AndroidAppWidgetManagerWrapper): AppWidgetManagerDomainWrapper

    @Binds
    @Singleton
    fun appWidgetHostWrapper(impl: AndroidAppWidgetHostWrapper): AppWidgetHostWrapper

    @Binds
    @Singleton
    fun appWidgetHostDomainWrapper(impl: AndroidAppWidgetHostWrapper): AppWidgetHostDomainWrapper
}