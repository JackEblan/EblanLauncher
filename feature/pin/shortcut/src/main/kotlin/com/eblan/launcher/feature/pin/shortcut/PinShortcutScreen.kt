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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinShortcutScreen(
    modifier: Modifier = Modifier,
    viewModel: PinShortcutViewModel = hiltViewModel(),
    pinItemRequest: LauncherApps.PinItemRequest,
    onHome: () -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    val launcherApps = LocalLauncherApps.current

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight(fraction = 0.5f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val shortcutInfo = pinItemRequest.shortcutInfo

                    if (shortcutInfo != null) {
                        val icon = remember {
                            launcherApps.getShortcutIconDrawable(
                                shortcutInfo = shortcutInfo,
                                density = 0,
                            )
                        }

                        AsyncImage(
                            modifier = Modifier
                                .dragAndDropSource(
                                    block = {
                                        detectTapGestures(
                                            onLongPress = {
                                                pinItemRequestWrapper.updatePinItemRequest(
                                                    pinItemRequest = pinItemRequest,
                                                )

                                                startTransfer(
                                                    DragAndDropTransferData(
                                                        clipData = ClipData.newPlainText(
                                                            "PinItemRequest", "PinItemRequest",
                                                        ),
                                                        flags = View.DRAG_FLAG_GLOBAL,
                                                    ),
                                                )

                                                onHome()
                                            },
                                        )
                                    },
                                )
                                .size(100.dp),
                            model = icon,
                            contentDescription = null,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Button(
                            onClick = {

                            },
                        ) {
                            Text(text = "Cancel")
                        }

                        Button(
                            onClick = {

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