package com.eblan.launcher.feature.pin

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PinScreen(
    modifier: Modifier = Modifier,
    viewModel: PinViewModel = hiltViewModel(),
    pinItemRequest: PinItemRequest,
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onAddedToHomeScreenToast: (String) -> Unit,
) {
    val gridItem by viewModel.gridItem.collectAsStateWithLifecycle()

    val isBoundWidget by viewModel.isBoundWidget.collectAsStateWithLifecycle()

    when (pinItemRequest.requestType) {
        PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
            PinWidgetScreen(
                modifier = modifier,
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                isBoundWidget = isBoundWidget,
                onDragStart = onDragStart,
                onFinish = onFinish,
                onAddWidgetToHomeScreen = viewModel::addPinWidgetToHomeScreen,
                onDeleteGridItem = viewModel::deleteGridItem,
                onAddedToHomeScreenToast = onAddedToHomeScreenToast,
                onUpdateWidgetGridItem = viewModel::updateGridItemData,
                onDeleteWidgetGridItem = viewModel::deleteWidgetGridItem,
            )
        }

        PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
            PinShortcutScreen(
                modifier = modifier,
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                onDragStart = onDragStart,
                onFinish = onFinish,
                onAddPinShortcutToHomeScreen = viewModel::addPinShortcutToHomeScreen,
                onDeleteShortcutGridItem = viewModel::deleteGridItem,
                onAddedToHomeScreenToast = onAddedToHomeScreenToast,
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
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onAddPinShortcutToHomeScreen: (
        id: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        byteArray: ByteArray,
    ) -> Unit,
    onDeleteShortcutGridItem: (GridItem) -> Unit,
    onAddedToHomeScreenToast: (String) -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    val launcherApps = LocalLauncherApps.current

    val shortcutInfo = pinItemRequest.shortcutInfo

    if (shortcutInfo != null) {
        val icon = remember {
            launcherApps.getShortcutIconDrawable(
                shortcutInfo = shortcutInfo,
                density = 0,
            )
        }

        LaunchedEffect(key1 = gridItem) {
            if (gridItem != null) {
                if (pinItemRequest.accept()) {
                    onAddedToHomeScreenToast(
                        """
                ${gridItem.page}
                ${gridItem.startRow}
                ${gridItem.startColumn}
            """.trimIndent(),
                    )
                } else {
                    onDeleteShortcutGridItem(gridItem)
                }
            }
        }

        Scaffold(containerColor = Color.Transparent) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
            ) {
                PinBottomSheet(
                    modifier = modifier,
                    icon = icon,
                    onAdd = {
                        onAddPinShortcutToHomeScreen(
                            shortcutInfo.id,
                            shortcutInfo.`package`,
                            shortcutInfo.shortLabel.toString(),
                            shortcutInfo.longLabel.toString(),
                            icon.toByteArray(),
                        )
                    },
                    onFinish = onFinish,
                    onLongPress = {
                        pinItemRequestWrapper.updatePinItemRequest(
                            pinItemRequest = pinItemRequest,
                        )

                        onDragStart()
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
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onAddWidgetToHomeScreen: (
        className: String,
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
    onDeleteGridItem: (GridItem) -> Unit,
    onAddedToHomeScreenToast: (String) -> Unit,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData.Widget,
    ) -> Unit,
    onDeleteWidgetGridItem: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    val appWidgetHostWrapper = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val context = LocalContext.current

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
                onFinish = onFinish,
                onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                onDeleteGridItem = { newGridItem ->
                    deleteAppWidgetId = true

                    onDeleteGridItem(newGridItem)
                },
            )
        }

        LaunchedEffect(key1 = gridItem) {
            handleGridItem(
                gridItem = gridItem,
                appWidgetHostWrapper = appWidgetHostWrapper,
                appWidgetManager = appWidgetManager,
                onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                onAddedToHomeScreenToast = onAddedToHomeScreenToast,
                onUpdateAppWidgetId = { newAppWidgetId ->
                    appWidgetId = newAppWidgetId
                },
                onLaunch = appWidgetLauncher::launch,
            )
        }

        LaunchedEffect(key1 = deleteAppWidgetId) {
            if (gridItem != null &&
                appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&
                deleteAppWidgetId
            ) {
                onDeleteWidgetGridItem(gridItem, appWidgetId)
            }
        }

        LaunchedEffect(key1 = isBoundWidget) {
            handleIsBoundWidget(
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                isBoundWidget = isBoundWidget,
                appWidgetId = appWidgetId,
                onDeleteGridItem = onDeleteGridItem,
                onFinish = onFinish,
            )
        }

        Scaffold(containerColor = Color.Transparent) { paddingValues ->
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
            ) {
                PinBottomSheet(
                    icon = icon,
                    onAdd = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            onAddWidgetToHomeScreen(
                                appWidgetProviderInfo.provider.className,
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
                                constraints.maxWidth,
                                constraints.maxHeight,
                            )
                        } else {
                            onAddWidgetToHomeScreen(
                                appWidgetProviderInfo.provider.className,
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
                    },
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PinBottomSheet(
    modifier: Modifier = Modifier,
    icon: Any,
    onAdd: suspend () -> Unit,
    onFinish: () -> Unit,
    onLongPress: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()

    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = {
                showBottomSheet = false
                onFinish()
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    modifier = Modifier
                        .dragAndDropSource(
                            block = {
                                detectTapGestures(
                                    onLongPress = {
                                        startTransfer(
                                            DragAndDropTransferData(
                                                clipData = ClipData.newPlainText(
                                                    "PinItemRequest", "PinItemRequest",
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion {
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

private fun handleGridItem(
    gridItem: GridItem?,
    appWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData.Widget,
    ) -> Unit,
    onAddedToHomeScreenToast: (String) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
) {
    if (gridItem == null) return

    val data = gridItem.data

    if (data is GridItemData.Widget) {
        val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

        onUpdateAppWidgetId(appWidgetId)

        onAddPinWidget(
            gridItem = gridItem,
            appWidgetId = appWidgetId,
            appWidgetManager = appWidgetManager,
            data = data,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            onLaunch = onLaunch,
        )

        onAddedToHomeScreenToast(
            """
                ${gridItem.page}
                ${gridItem.startRow}
                ${gridItem.startColumn}
            """.trimIndent(),
        )
    }
}

private fun onAddPinWidget(
    gridItem: GridItem,
    appWidgetId: Int,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    data: GridItemData.Widget,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData.Widget,
    ) -> Unit,
    onLaunch: (Intent) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(data.componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateWidgetGridItem(gridItem.id, newData)
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunch(intent)
    }
}

private fun handleAppWidgetLauncherResult(
    gridItem: GridItem?,
    result: ActivityResult,
    onFinish: () -> Unit,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData.Widget,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val data = (gridItem?.data as? GridItemData.Widget) ?: return

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId =
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateWidgetGridItem(gridItem.id, newData)
    } else {
        onDeleteGridItem(gridItem)

        onFinish()
    }
}

private fun handleIsBoundWidget(
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest?,
    isBoundWidget: Boolean,
    appWidgetId: Int,
    onDeleteGridItem: (GridItem) -> Unit,
    onFinish: () -> Unit,
) {
    if (gridItem != null && isBoundWidget) {
        bindPinWidget(
            gridItem = gridItem,
            pinItemRequest = pinItemRequest,
            appWidgetId = appWidgetId,
            onDeleteGridItem = onDeleteGridItem,
            onFinish = onFinish,
        )
    }
}

private fun bindPinWidget(
    gridItem: GridItem,
    pinItemRequest: PinItemRequest?,
    appWidgetId: Int,
    onDeleteGridItem: (GridItem) -> Unit,
    onFinish: () -> Unit,
) {
    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept(extras)
    ) {
        onFinish()
    } else {
        onDeleteGridItem(gridItem)
    }
}
