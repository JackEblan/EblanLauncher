package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.ApplicationInfoDao
import com.eblan.launcher.data.room.entity.EblanLauncherApplicationInfoEntity
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanLauncherApplicationInfo
import com.eblan.launcher.domain.repository.ApplicationInfoRepository
import javax.inject.Inject

class DefaultApplicationInfoRepository @Inject constructor(
    private val applicationInfoDao: ApplicationInfoDao,
    private val packageManagerWrapper: PackageManagerWrapper,
) : ApplicationInfoRepository {
    override suspend fun getApplicationInfo(gridItemId: Int): EblanLauncherApplicationInfo {
        return applicationInfoDao.getApplicationInfoEntity(gridItemId = gridItemId)
            .toApplicationInfo()
    }

    override suspend fun upsertApplicationInfo(
        gridItemId: Int,
        applicationInfo: EblanLauncherApplicationInfo,
    ) {
        applicationInfoDao.upsertApplicationInfoEntity(
            applicationInfoEntity = applicationInfo.toEntity(
                gridItemId = gridItemId,
            ),
        )
    }

    private suspend fun EblanLauncherApplicationInfoEntity.toApplicationInfo(): EblanLauncherApplicationInfo {
        return EblanLauncherApplicationInfo(
            id = id,
            gridItemId = gridItemId,
            packageName = packageName,
            flags = flags,
            icon = packageManagerWrapper.getApplicationIcon(packageName = packageName),
            label = label,
        )
    }

    private fun EblanLauncherApplicationInfo.toEntity(gridItemId: Int): EblanLauncherApplicationInfoEntity {
        return EblanLauncherApplicationInfoEntity(
            id = id,
            gridItemId = gridItemId,
            packageName = packageName,
            flags = flags,
            label = label,
        )
    }
}