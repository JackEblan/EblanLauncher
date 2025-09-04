package com.eblan.launcher.framework.resources

import com.eblan.launcher.domain.framework.ResourcesWrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ResourcesModule {

    @Binds
    @Singleton
    fun resourcesWrapper(impl: DefaultResourcesWrapper): ResourcesWrapper
}