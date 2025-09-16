package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.EblanIconPackInfoEntity
import com.eblan.launcher.domain.model.EblanIconPackInfo

fun EblanIconPackInfo.asEntity(): EblanIconPackInfoEntity {
    return EblanIconPackInfoEntity(
        packageName = packageName,
        icon = icon,
        label = label,
    )
}

fun EblanIconPackInfoEntity.asModel(): EblanIconPackInfo {
    return EblanIconPackInfo(
        packageName = packageName,
        icon = icon,
        label = label,
    )
}