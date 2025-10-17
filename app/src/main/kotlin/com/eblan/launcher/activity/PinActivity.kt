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
package com.eblan.launcher.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.feature.pin.PinScreen
import com.eblan.launcher.framework.bitmap.AndroidBitmapWrapper
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.model.PinActivityUiState
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalBitmap
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.viewmodel.PinActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PinActivity : ComponentActivity() {

    @Inject
    lateinit var androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper

    @Inject
    lateinit var androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper

    @Inject
    lateinit var androidLauncherAppsWrapper: AndroidLauncherAppsWrapper

    @Inject
    lateinit var pinItemRequestWrapper: PinItemRequestWrapper

    @Inject
    lateinit var androidBitmapWrapper: AndroidBitmapWrapper

    private val viewModel: PinActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(packageName)

            val pinItemRequest = androidLauncherAppsWrapper.getPinItemRequest(intent = intent)

            setContent {
                CompositionLocalProvider(
                    LocalAppWidgetHost provides androidAppWidgetHostWrapper,
                    LocalAppWidgetManager provides androidAppWidgetManagerWrapper,
                    LocalPinItemRequest provides pinItemRequestWrapper,
                    LocalLauncherApps provides androidLauncherAppsWrapper,
                    LocalBitmap provides androidBitmapWrapper,
                ) {
                    val pinActivityUiState by viewModel.pinActivityUiState.collectAsStateWithLifecycle()

                    when (val state = pinActivityUiState) {
                        PinActivityUiState.Loading -> {
                        }

                        is PinActivityUiState.Success -> {
                            EblanLauncherTheme(
                                themeBrand = state.themeSettings.themeBrand,
                                darkThemeConfig = state.themeSettings.darkThemeConfig,
                                dynamicTheme = state.themeSettings.dynamicTheme,
                            ) {
                                PinScreen(
                                    pinItemRequest = pinItemRequest,
                                    onDragStart = {
                                        startActivity(homeIntent)

                                        finish()
                                    },
                                    onFinish = ::finish,
                                    onAddedToHomeScreenToast = { message ->
                                        Toast.makeText(
                                            applicationContext,
                                            message,
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
