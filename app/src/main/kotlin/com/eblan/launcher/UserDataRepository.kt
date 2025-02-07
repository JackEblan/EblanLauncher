package com.eblan.launcher

import com.eblan.launcher.domain.model.UserData
import kotlinx.coroutines.flow.flowOf

class UserDataRepository {
    val userData = flowOf(UserData(rows = 4, columns = 4))
}