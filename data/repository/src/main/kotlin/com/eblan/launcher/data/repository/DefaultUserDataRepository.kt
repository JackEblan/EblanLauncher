package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

internal class DefaultUserDataRepository @Inject constructor() : UserDataRepository {
    override val userData = flowOf(UserData(rows = 10, columns = 10, pageCount = 3))
}