package com.eblan.launcher.repository

import com.eblan.launcher.domain.model.UserData
import kotlinx.coroutines.flow.flowOf

class UserDataRepository {
    val userData = flowOf(UserData(rows = 50, columns = 50))
}