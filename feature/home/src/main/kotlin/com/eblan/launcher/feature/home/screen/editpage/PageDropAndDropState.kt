package com.eblan.launcher.feature.home.screen.editpage

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
fun rememberGridDragAndDropState(
    gridState: LazyGridState,
    onMove: (
        from: Int,
        to: Int,
    ) -> Unit,
): GridDragAndDropState {
    val scope = rememberCoroutineScope()

    val state =
        remember(key1 = gridState) {
            GridDragAndDropState(
                state = gridState,
                onMove = onMove,
                scope = scope,
            )
        }
    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            gridState.scrollBy(diff)
        }
    }
    return state
}

class GridDragAndDropState internal constructor(
    private val state: LazyGridState,
    private val scope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>()

    private var draggingItemDraggedDelta by mutableStateOf(Offset.Zero)
    private var draggingItemInitialOffset by mutableStateOf(Offset.Zero)
    internal val draggingItemOffset: Offset
        get() =
            draggingItemLayoutInfo?.let { item ->
                draggingItemInitialOffset + draggingItemDraggedDelta - item.offset.toOffset()
            } ?: Offset.Zero

    private val draggingItemLayoutInfo: LazyGridItemInfo?
        get() = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set

    internal var previousItemOffset = Animatable(Offset.Zero, Offset.VectorConverter)
        private set

    internal fun onDragStart(offset: Offset) {
        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                offset.x.toInt() in item.offset.x..item.offsetEnd.x &&
                        offset.y.toInt() in item.offset.y..item.offsetEnd.y
            }
            ?.also {
                draggingItemIndex = it.index
                draggingItemInitialOffset = it.offset.toOffset()
            }
    }

    internal fun onDragInterrupted() {
        if (draggingItemIndex != null) {
            previousIndexOfDraggedItem = draggingItemIndex
            val startOffset = draggingItemOffset
            scope.launch {
                previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    Offset.Zero,
                    spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = Offset.VisibilityThreshold,
                    ),
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemDraggedDelta = Offset.Zero
        draggingItemIndex = null
        draggingItemInitialOffset = Offset.Zero
    }

    internal fun onDrag(offset: Offset) {
        draggingItemDraggedDelta += offset

        val draggingItem = draggingItemLayoutInfo ?: return
        val startOffset = draggingItem.offset.toOffset() + draggingItemOffset
        val endOffset = startOffset + draggingItem.size.toSize()
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem =
            state.layoutInfo.visibleItemsInfo.find { item ->
                middleOffset.x.toInt() in item.offset.x..item.offsetEnd.x &&
                        middleOffset.y.toInt() in item.offset.y..item.offsetEnd.y &&
                        draggingItem.index != item.index
            }
        if (targetItem != null) {
            if (
                draggingItem.index == state.firstVisibleItemIndex ||
                targetItem.index == state.firstVisibleItemIndex
            ) {
                state.requestScrollToItem(
                    state.firstVisibleItemIndex,
                    state.firstVisibleItemScrollOffset,
                )
            }
            onMove.invoke(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
        } else {
            val overscroll =
                when {
                    draggingItemDraggedDelta.y > 0 ->
                        (endOffset.y - state.layoutInfo.viewportEndOffset).coerceAtLeast(0f)

                    draggingItemDraggedDelta.y < 0 ->
                        (startOffset.y - state.layoutInfo.viewportStartOffset).coerceAtMost(0f)

                    else -> 0f
                }
            if (overscroll != 0f) {
                scrollChannel.trySend(overscroll)
            }
        }
    }

    private val LazyGridItemInfo.offsetEnd: IntOffset
        get() = this.offset + this.size
}

private operator fun IntOffset.plus(size: IntSize): IntOffset {
    return IntOffset(x + size.width, y + size.height)
}

private operator fun Offset.plus(size: Size): Offset {
    return Offset(x + size.width, y + size.height)
}

fun Modifier.dragContainer(state: GridDragAndDropState): Modifier {
    return pointerInput(state) {
        detectDragGesturesAfterLongPress(
            onDrag = { change, offset ->
                change.consume()
                state.onDrag(offset = offset)
            },
            onDragStart = { offset -> state.onDragStart(offset) },
            onDragEnd = { state.onDragInterrupted() },
            onDragCancel = { state.onDragInterrupted() },
        )
    }
}

@Composable
fun LazyGridItemScope.DraggableItem(
    modifier: Modifier = Modifier,
    state: GridDragAndDropState,
    index: Int,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    val dragging = index == state.draggingItemIndex

    val draggingModifier =
        if (dragging) {
            Modifier
                .zIndex(1f)
                .graphicsLayer {
                    translationX = state.draggingItemOffset.x
                    translationY = state.draggingItemOffset.y
                }
        } else if (index == state.previousIndexOfDraggedItem) {
            Modifier
                .zIndex(1f)
                .graphicsLayer {
                    translationX = state.previousItemOffset.value.x
                    translationY = state.previousItemOffset.value.y
                }
        } else {
            Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
        }
    Box(modifier = modifier.then(draggingModifier), propagateMinConstraints = true) {
        content(dragging)
    }
}