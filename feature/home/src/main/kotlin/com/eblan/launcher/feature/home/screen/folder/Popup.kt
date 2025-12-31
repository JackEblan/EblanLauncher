package com.eblan.launcher.feature.home.screen.folder

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.FolderPopupType
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.FolderPopup(
    modifier: Modifier = Modifier,
    foldersDataById: ArrayDeque<FolderDataById>,
    popupIntOffset: IntOffset,
    popupIntSize: IntSize,
    hasShortcutHostPermission: Boolean,
    drag: Drag,
    paddingValues: PaddingValues,
    textColor: TextColor,
    homeSettings: HomeSettings,
    folderGridHorizontalPagerState: PagerState,
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String>,
    screenWidth: Int,
    screenHeight: Int,
    folderPopupType: FolderPopupType,
    onDismissRequest: () -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val density = LocalDensity.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }
    val x = popupIntOffset.x - leftPadding

    val y = popupIntOffset.y - topPadding

    Layout(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    awaitRelease()

                    onDismissRequest()
                })
            }
            .fillMaxSize()
            .padding(paddingValues),
        content = {
            FolderScreen(
                foldersDataById = foldersDataById,
                drag = drag,
                hasShortcutHostPermission = hasShortcutHostPermission,
                textColor = textColor,
                homeSettings = homeSettings,
                folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                statusBarNotifications = statusBarNotifications,
                iconPackFilePaths = iconPackFilePaths,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                paddingValues = paddingValues,
                onLongPressGridItem = onLongPressGridItem,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onDraggingGridItem = onDraggingGridItem,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
            )
        },
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
            ),
        )

        val parentCenterX = x + popupIntSize.width / 2

        val childX = (parentCenterX - placeable.width / 2)
            .coerceIn(0, constraints.maxWidth - placeable.width)

        val topY = y - placeable.height
        val bottomY = y + popupIntSize.height

        val childY = if (topY < 0) bottomY else topY

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                x = childX,
                y = childY,
            )
        }
    }
}
