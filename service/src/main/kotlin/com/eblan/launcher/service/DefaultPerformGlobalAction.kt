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
package com.eblan.launcher.service

import com.eblan.launcher.domain.framework.PerformGlobalAction
import com.eblan.launcher.domain.model.GlobalAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

internal class DefaultPerformGlobalAction @Inject constructor() : PerformGlobalAction {
    private val _globalAction = MutableSharedFlow<GlobalAction>()

    override val globalAction = _globalAction.asSharedFlow()

    override suspend fun performGlobalAction(globalAction: GlobalAction) {
        _globalAction.emit(globalAction)
    }
}
