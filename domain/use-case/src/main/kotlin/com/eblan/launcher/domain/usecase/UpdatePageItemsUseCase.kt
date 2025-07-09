package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.AppWidgetHostDomainWrapper
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.PageCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePageItemsUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val appWidgetHostDomainWrapper: AppWidgetHostDomainWrapper,
    private val pageCacheRepository: PageCacheRepository,
) {
    suspend operator fun invoke(
        initialPage: Int,
        pageItems: List<PageItem>,
    ) {
        withContext(Dispatchers.Default) {
            pageCacheRepository.pageItemsToDelete.first().forEach { pageItem ->
                gridRepository.deleteGridItemEntities(gridItems = pageItem.gridItems)

                pageItem.gridItems.forEach { gridItem ->
                    val data = gridItem.data

                    if (data is GridItemData.Widget) {
                        appWidgetHostDomainWrapper.deleteAppWidgetId(appWidgetId = data.appWidgetId)
                    }
                }
            }

            val gridItems = pageItems.mapIndexed { index, pageItem ->
                pageItem.gridItems.map { gridItem ->
                    gridItem.copy(page = index)
                }
            }.flatten()

            gridRepository.upsertGridItems(gridItems = gridItems)

            userDataRepository.updateInitialPage(initialPage = initialPage)

            userDataRepository.updatePageCount(pageCount = pageItems.size)
        }
    }
}