package com.eblan.launcher.feature.pin.widget

import android.content.ClipData
import android.view.View
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.unit.dp

@Composable
fun PinWidgetScreen(
    modifier: Modifier = Modifier,
    onHome: () -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier = modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                modifier = Modifier
                    .dragAndDropSource { _ ->
                        DragAndDropTransferData(
                            clipData = ClipData.newPlainText(
                                "Message", "Hello",
                            ),
                            flags = View.DRAG_FLAG_GLOBAL,
                        ).also {
                            onHome()
                        }
                    }
                    .size(100.dp),
            )
        }
    }
}