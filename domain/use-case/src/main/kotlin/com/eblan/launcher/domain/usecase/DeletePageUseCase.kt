package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeletePageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke(page: Int) {
        val userData = userDataRepository.userData.first()

        userDataRepository.updatePageCount(pageCount = userData.pageCount - 1)

        gridRepository.deleteItemsOnPage(page = page)

        gridRepository.shiftPagesAfterDeletedPage(page = page)
    }
}