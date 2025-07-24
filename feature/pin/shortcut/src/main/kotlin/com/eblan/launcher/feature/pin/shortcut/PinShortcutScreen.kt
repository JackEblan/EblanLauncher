package com.eblan.launcher.feature.pin.shortcut

import android.content.ClipData
import android.content.pm.LauncherApps
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PinShortcutScreen(
    modifier: Modifier = Modifier,
    viewModel: PinShortcutViewModel = hiltViewModel(),
    pinItemRequest: LauncherApps.PinItemRequest,
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onAddedToHomeScreen: (String) -> Unit,
) {
    val addedGridItem by viewModel.addedGridItem.collectAsStateWithLifecycle()

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

        LaunchedEffect(key1 = addedGridItem) {
            addedGridItem?.let { gridItem ->
                if (pinItemRequest.accept()) {
                    onAddedToHomeScreen(
                        """
                ${gridItem.startRow}
                ${gridItem.startColumn}
            """.trimIndent(),
                    )
                } else {
                    viewModel.deleteGridItem(gridItem = gridItem)
                }
            }
        }

        PinShortcutScreen(
            modifier = modifier,
            icon = icon,
            onAdd = {
                viewModel.addToHomeScreen(
                    id = shortcutInfo.id,
                    packageName = shortcutInfo.`package`,
                    shortLabel = shortcutInfo.shortLabel.toString(),
                    longLabel = shortcutInfo.longLabel.toString(),
                    byteArray = icon.toByteArray(),
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PinShortcutScreen(
    modifier: Modifier = Modifier,
    icon: Any,
    onAdd: suspend () -> Unit,
    onFinish: () -> Unit,
    onLongPress: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()

    val scope = rememberCoroutineScope()

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
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
    }
}