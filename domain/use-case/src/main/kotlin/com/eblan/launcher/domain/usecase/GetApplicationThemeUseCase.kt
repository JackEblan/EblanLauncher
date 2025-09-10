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
package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.model.ApplicationTheme
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetApplicationThemeUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    private val resourcesWrapper: ResourcesWrapper,
) {
    operator fun invoke(): Flow<ApplicationTheme> {
        return combine(
            userDataRepository.userData,
            wallpaperManagerWrapper.getColorsChanged(),
        ) { userData, colorHints ->
            getApplicationTheme(
                themeBrand = userData.generalSettings.themeBrand,
                darkThemeConfig = userData.generalSettings.darkThemeConfig,
                dynamicTheme = userData.generalSettings.dynamicTheme,
                colorHints = colorHints,
            )
        }
    }

    private fun getApplicationTheme(
        themeBrand: ThemeBrand,
        darkThemeConfig: DarkThemeConfig,
        dynamicTheme: Boolean,
        colorHints: Int?,
    ): ApplicationTheme {
        return when (darkThemeConfig) {
            DarkThemeConfig.System -> {
                if (colorHints != null) {
                    val hintSupportsDarkTheme =
                        colorHints.and(wallpaperManagerWrapper.hintSupportsDarkTheme) != 0

                    if (hintSupportsDarkTheme) {
                        ApplicationTheme(
                            themeBrand = themeBrand,
                            darkThemeConfig = DarkThemeConfig.Dark,
                            dynamicTheme = dynamicTheme,
                        )
                    } else {
                        ApplicationTheme(
                            themeBrand = themeBrand,
                            darkThemeConfig = DarkThemeConfig.Light,
                            dynamicTheme = dynamicTheme,
                        )
                    }
                } else {
                    ApplicationTheme(
                        themeBrand = themeBrand,
                        darkThemeConfig = resourcesWrapper.getSystemTheme(),
                        dynamicTheme = dynamicTheme,
                    )
                }
            }

            DarkThemeConfig.Light, DarkThemeConfig.Dark -> {
                ApplicationTheme(
                    themeBrand = themeBrand,
                    darkThemeConfig = darkThemeConfig,
                    dynamicTheme = dynamicTheme,
                )
            }
        }
    }
}
