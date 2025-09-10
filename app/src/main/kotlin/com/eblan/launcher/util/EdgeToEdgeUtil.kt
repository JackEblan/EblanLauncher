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
package com.eblan.launcher.util

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.eblan.launcher.domain.model.DarkThemeConfig

fun ComponentActivity.handleEdgeToEdge(darkThemeConfig: DarkThemeConfig) {
    when (darkThemeConfig) {
        DarkThemeConfig.System -> {
            enableEdgeToEdge()
        }

        DarkThemeConfig.Light -> {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
                navigationBarStyle = SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
            )
        }

        DarkThemeConfig.Dark -> {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
            )
        }
    }
}
