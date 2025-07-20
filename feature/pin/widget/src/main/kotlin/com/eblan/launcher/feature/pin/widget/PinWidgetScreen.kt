package com.eblan.launcher.feature.pin.widget

import android.content.ClipData
import android.content.pm.LauncherApps
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalPinItemRequest

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinWidgetScreen(
    modifier: Modifier = Modifier,
    pinItemRequest: LauncherApps.PinItemRequest,
    onHome: () -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    val context = LocalContext.current

    val appWidgetProviderInfo = pinItemRequest.getAppWidgetProviderInfo(context)

    val preview = remember {
        appWidgetProviderInfo?.loadPreviewImage(context, 0)
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                modifier = Modifier
                    .dragAndDropSource(
                        block = {
                            detectTapGestures(
                                onLongPress = {
                                    pinItemRequestWrapper.updatePinItemRequest(pinItemRequest = pinItemRequest)

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
                model = preview,
                contentDescription = null,
            )
        }
    }
}