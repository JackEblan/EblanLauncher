package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.domain.model.EblanApplicationInfo

fun EblanApplicationInfo.asEntity(): EblanApplicationInfoEntity {
    return EblanApplicationInfoEntity(
        packageName = packageName,
        componentName = componentName,
        icon = icon,
        label = label,
    )
}

fun EblanApplicationInfoEntity.asModel(): EblanApplicationInfo {
    return EblanApplicationInfo(
        packageName = packageName,
        componentName = componentName,
        icon = icon,
        label = label,
    )
}