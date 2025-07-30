package com.eblan.launcher.feature.home.component.overscroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

class OffsetOverscrollEffect(
    private val scope: CoroutineScope,
    private val overscrollAlpha: Animatable<Float, AnimationVector1D>,
    private val onDragEnd: suspend () -> Unit,
) : OverscrollEffect {
    private val overscrollOffset = Animatable(0f)

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        // in pre scroll we relax the overscroll if needed
        // relaxation: when we are in progress of the overscroll and user scrolls in the
        // different direction = subtract the overscroll first
        val sameDirection = sign(delta.y) == sign(overscrollOffset.value)
        val consumedByPreScroll =
            if (abs(overscrollOffset.value) > 0.5 && !sameDirection) {
                val prevOverscrollValue = overscrollOffset.value
                val newOverscrollValue = overscrollOffset.value + delta.y
                if (sign(prevOverscrollValue) != sign(newOverscrollValue)) {
                    // sign changed, coerce to start scrolling and exit
                    scope.launch {
                        overscrollOffset.snapTo(0f)
                        overscrollAlpha.snapTo(0f)
                    }
                    Offset(x = 0f, y = delta.y + prevOverscrollValue)
                } else {
                    scope.launch {
                        overscrollOffset.snapTo(overscrollOffset.value + delta.y)
                        overscrollAlpha.snapTo(overscrollOffset.value + delta.y)
                    }

                    delta.copy(x = 0f)
                }
            } else {
                Offset.Zero
            }
        val leftForScroll = delta - consumedByPreScroll
        val consumedByScroll = performScroll(leftForScroll)
        val overscrollDelta = leftForScroll - consumedByScroll
        // if it is a drag, not a fling, add the delta left to our over scroll value
        if (abs(overscrollDelta.y) > 0.5 && source == NestedScrollSource.UserInput) {
            scope.launch {
                val newOffset = overscrollOffset.value + overscrollDelta.y * 0.1f

                overscrollOffset.snapTo(newOffset)

                if (newOffset > 0f) {
                    overscrollAlpha.snapTo(newOffset)
                } else {
                    overscrollAlpha.snapTo(0f)
                }
            }
        }

        return consumedByPreScroll + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        val consumed = performFling(velocity)

        val remaining = velocity - consumed

        val alpha = 1f - (abs(overscrollAlpha.value) / 100f)

        if (alpha < 0.2f) {
            onDragEnd()
        }

        overscrollAlpha.snapTo(0f)

        overscrollOffset.animateTo(
            targetValue = 0f,
            initialVelocity = remaining.y,
            animationSpec = spring(),
        )
    }

    override val isInProgress: Boolean
        get() = overscrollOffset.value != 0f

    // Create a LayoutModifierNode that offsets by overscrollOffset.value
    override val node: DelegatableNode =
        object : Modifier.Node(), LayoutModifierNode {
            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints,
            ): MeasureResult {
                val placeable = measurable.measure(constraints)
                return layout(placeable.width, placeable.height) {
                    val offsetValue = IntOffset(x = 0, y = overscrollOffset.value.roundToInt())
                    placeable.placeRelativeWithLayer(offsetValue.x, offsetValue.y)
                }
            }
        }
}
