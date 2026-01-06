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
package com.eblan.launcher.feature.pin

import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalByteArray
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PinScreen(
    modifier: Modifier = Modifier,
    viewModel: PinScreenViewModel = hiltViewModel(),
    pinItemRequest: PinItemRequest,
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
) {
    val gridItem by viewModel.gridItem.collectAsStateWithLifecycle()

    val isBoundWidget by viewModel.isBoundWidget.collectAsStateWithLifecycle()

    val isFinished by viewModel.isFinished.collectAsStateWithLifecycle()

    when (pinItemRequest.requestType) {
        PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
            PinWidgetScreen(
                modifier = modifier,
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                isBoundWidget = isBoundWidget,
                isFinished = isFinished,
                onDragStart = onDragStart,
                onFinish = onFinish,
                onAddPinWidgetToHomeScreen = viewModel::addPinWidgetToHomeScreen,
                onDeleteGridItemCache = viewModel::deleteGridItemCache,
                onUpdateGridItemCache = viewModel::updateGridItemDataCache,
                onUpdateGridItems = viewModel::updateGridItems,
            )
        }

        PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
            PinShortcutScreen(
                modifier = modifier,
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                isFinished = isFinished,
                onDragStart = onDragStart,
                onFinish = onFinish,
                onAddPinShortcutToHomeScreen = viewModel::addPinShortcutToHomeScreen,
                onDeleteShortcutGridItem = viewModel::deleteGridItemCache,
                onUpdateGridItems = viewModel::updateGridItems,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PinShortcutScreen(
    modifier: Modifier = Modifier,
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest,
    isFinished: Boolean,
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onAddPinShortcutToHomeScreen: (
        serialNumber: Long,
        id: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        isEnabled: Boolean,
        disabledMessage: String?,
        byteArray: ByteArray?,
    ) -> Unit,
    onDeleteShortcutGridItem: (GridItem) -> Unit,
    onUpdateGridItems: () -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    val launcherApps = LocalLauncherApps.current

    val byteArrayWrapper = LocalByteArray.current

    val shortcutInfo = pinItemRequest.shortcutInfo

    val userManager = LocalUserManager.current

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    if (shortcutInfo != null) {
        val icon = remember {
            launcherApps.getShortcutIconDrawable(
                shortcutInfo = shortcutInfo,
                density = 0,
            )
        }

        LaunchedEffect(key1 = gridItem) {
            if (gridItem == null) return@LaunchedEffect

            if (pinItemRequest.isValid && pinItemRequest.accept()) {
                Toast.makeText(
                    context,
                    """
                ${gridItem.page}
                ${gridItem.startRow}
                ${gridItem.startColumn}
                    """.trimIndent(),
                    Toast.LENGTH_LONG,
                ).show()

                onUpdateGridItems()
            } else {
                onDeleteShortcutGridItem(gridItem)
            }
        }

        LaunchedEffect(key1 = isFinished) {
            if (isFinished) {
                onFinish()
            }
        }

        Scaffold(containerColor = Color.Transparent) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                PinBottomSheet(
                    label = shortcutInfo.shortLabel.toString(),
                    icon = icon,
                    onAdd = {
                        scope.launch {
                            onAddPinShortcutToHomeScreen(
                                userManager.getSerialNumberForUser(userHandle = shortcutInfo.userHandle),
                                shortcutInfo.id,
                                shortcutInfo.`package`,
                                shortcutInfo.shortLabel.toString(),
                                shortcutInfo.longLabel.toString(),
                                shortcutInfo.isEnabled,
                                shortcutInfo.disabledMessage?.toString(),
                                icon?.let {
                                    byteArrayWrapper.createByteArray(drawable = it)
                                },
                            )
                        }
                    },
                    onFinish = onFinish,
                    onLongPress = {
                        pinItemRequestWrapper.updatePinItemRequest(
                            pinItemRequest = pinItemRequest,
                        )

                        onDragStart()

                        onFinish()
                    },
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PinWidgetScreen(
    modifier: Modifier = Modifier,
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest,
    isBoundWidget: Boolean,
    isFinished: Boolean,
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onAddPinWidgetToHomeScreen: (
        serialNumber: Long,
        componentName: String,
        configure: String?,
        packageName: String,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
    ) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemCache: (GridItem) -> Unit,
    onUpdateGridItems: () -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    val appWidgetHostWrapper = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val userManager = LocalUserManager.current

    val context = LocalContext.current

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    val appWidgetProviderInfo = pinItemRequest.getAppWidgetProviderInfo(context)

    var appWidgetId by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }

    var deleteAppWidgetId by remember { mutableStateOf(false) }

    if (appWidgetProviderInfo != null) {
        val icon = remember {
            appWidgetProviderInfo.loadPreviewImage(context, 0)
        }

        val appWidgetLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            handleAppWidgetLauncherResult(
                gridItem = gridItem,
                result = result,
                onUpdateGridItemCache = onUpdateGridItemCache,
                onDeleteAppWidgetId = {
                    deleteAppWidgetId = true
                },
            )
        }

        LaunchedEffect(key1 = gridItem) {
            handleGridItem(
                gridItem = gridItem,
                appWidgetHostWrapper = appWidgetHostWrapper,
                appWidgetManager = appWidgetManager,
                userHandle = appWidgetProviderInfo.profile,
                onUpdateGridItemCache = onUpdateGridItemCache,
                onAddedToHomeScreenToast = { message ->
                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_LONG,
                    ).show()
                },
                onUpdateAppWidgetId = { newAppWidgetId ->
                    appWidgetId = newAppWidgetId
                },
                onLaunch = appWidgetLauncher::launch,
            )
        }

        LaunchedEffect(key1 = deleteAppWidgetId) {
            handleDeleteAppWidgetId(
                gridItem = gridItem,
                appWidgetId = appWidgetId,
                deleteAppWidgetId = deleteAppWidgetId,
                onDeleteGridItem = onDeleteGridItemCache,
            )
        }

        LaunchedEffect(key1 = isBoundWidget) {
            handleIsBoundWidget(
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                isBoundWidget = isBoundWidget,
                appWidgetId = appWidgetId,
                onDeleteGridItem = onDeleteGridItemCache,
                onUpdateGridItems = onUpdateGridItems,
            )
        }

        LaunchedEffect(key1 = isFinished) {
            if (isFinished) {
                onFinish()
            }
        }

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PinBottomSheet(
                label = appWidgetProviderInfo.loadLabel(context.packageManager),
                icon = icon,
                onAdd = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        onAddPinWidgetToHomeScreen(
                            userManager.getSerialNumberForUser(userHandle = appWidgetProviderInfo.profile),
                            appWidgetProviderInfo.provider.flattenToString(),
                            appWidgetProviderInfo.configure.flattenToString(),
                            appWidgetProviderInfo.provider.packageName,
                            appWidgetProviderInfo.targetCellHeight,
                            appWidgetProviderInfo.targetCellWidth,
                            appWidgetProviderInfo.minWidth,
                            appWidgetProviderInfo.minHeight,
                            appWidgetProviderInfo.resizeMode,
                            appWidgetProviderInfo.minResizeWidth,
                            appWidgetProviderInfo.minResizeHeight,
                            appWidgetProviderInfo.maxResizeWidth,
                            appWidgetProviderInfo.maxResizeHeight,
                            this@BoxWithConstraints.constraints.maxWidth,
                            this@BoxWithConstraints.constraints.maxHeight,
                        )
                    } else {
                        onAddPinWidgetToHomeScreen(
                            userManager.getSerialNumberForUser(userHandle = appWidgetProviderInfo.profile),
                            appWidgetProviderInfo.provider.flattenToString(),
                            appWidgetProviderInfo.configure.flattenToString(),
                            appWidgetProviderInfo.provider.packageName,
                            0,
                            0,
                            appWidgetProviderInfo.minWidth,
                            appWidgetProviderInfo.minHeight,
                            appWidgetProviderInfo.resizeMode,
                            appWidgetProviderInfo.minResizeWidth,
                            appWidgetProviderInfo.minResizeHeight,
                            0,
                            0,
                            constraints.maxWidth,
                            constraints.maxHeight,
                        )
                    }
                },
                onFinish = onFinish,
                onLongPress = {
                    pinItemRequestWrapper.updatePinItemRequest(
                        pinItemRequest = pinItemRequest,
                    )

                    onDragStart()

                    onFinish()
                },
            )
        }
    }
}

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PinBottomSheet(
    modifier: Modifier = Modifier,
    label: String,
    icon: Any?,
    onAdd: suspend () -> Unit,
    onFinish: () -> Unit,
    onLongPress: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()

    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false

                onFinish()
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Touch and hold the widget to move it around the home screen",
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                AsyncImage(
                    modifier = Modifier
                        .dragAndDropSource(
                            block = {
                                detectTapGestures(
                                    onLongPress = {
                                        startTransfer(
                                            DragAndDropTransferData(
                                                clipData = ClipData.newPlainText(
                                                    "PinItemRequest",
                                                    "PinItemRequest",
                                                ),
                                                flags = View.DRAG_FLAG_GLOBAL,
                                            ),
                                        )

                                        onLongPress()
                                    },
                                )
                            },
                        )
                        .size(100.dp),
                    model = icon,
                    contentDescription = null,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false

                                    onFinish()
                                }
                            }
                        },
                    ) {
                        Text(text = "Cancel")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                onAdd()
                            }
                        },
                    ) {
                        Text(text = "Add")
                    }
                }
            }
        }
    }
}
