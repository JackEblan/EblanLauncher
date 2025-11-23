/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.framework.usermanager

import android.content.Context
import android.content.Context.USER_SERVICE
import android.os.UserHandle
import android.os.UserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultUserManagerWrapper @Inject constructor(@param:ApplicationContext private val context: Context) : AndroidUserManagerWrapper {
    private val userManager = context.getSystemService(USER_SERVICE) as UserManager

    override fun getSerialNumberForUser(userHandle: UserHandle): Long {
        return userManager.getSerialNumberForUser(userHandle)
    }

    override fun getUserForSerialNumber(serialNumber: Long): UserHandle? {
        return userManager.getUserForSerialNumber(serialNumber)
    }
}
