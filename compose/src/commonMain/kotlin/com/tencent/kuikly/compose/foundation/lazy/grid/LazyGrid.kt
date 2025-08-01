/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.kuikly.compose.foundation.lazy.grid

import com.tencent.kuikly.compose.foundation.ExperimentalFoundationApi
import com.tencent.kuikly.compose.foundation.checkScrollableContainerConstraints
import com.tencent.kuikly.compose.foundation.gestures.Orientation
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.calculateEndPadding
import com.tencent.kuikly.compose.foundation.layout.calculateStartPadding
import com.tencent.kuikly.compose.foundation.lazy.layout.LazyLayout
import com.tencent.kuikly.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import com.tencent.kuikly.compose.foundation.lazy.layout.calculateLazyLayoutPinnedIndices
import com.tencent.kuikly.compose.foundation.lazy.layout.lazyLayoutBeyondBoundsModifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import com.tencent.kuikly.compose.scroller.kuiklyInfo
import com.tencent.kuikly.compose.scroller.tryExpandStartSizeNoScroll
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.MeasureResult
import com.tencent.kuikly.compose.ui.layout.Placeable
import com.tencent.kuikly.compose.ui.platform.LocalLayoutDirection
import com.tencent.kuikly.compose.ui.unit.Constraints
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.unit.constrainHeight
import com.tencent.kuikly.compose.ui.unit.constrainWidth
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.offset
import com.tencent.kuikly.compose.ui.util.fastForEach
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyGrid(
    /** Modifier to be applied for the inner layout */
    modifier: Modifier = Modifier,
    /** State controlling the scroll position */
    state: LazyGridState,
    /** Prefix sums of cross axis sizes of slots per line, e.g. the columns for vertical grid. */
    slots: LazyGridSlotsProvider,
    /** The inner padding to be added for the whole content (not for each individual item) */
    contentPadding: PaddingValues = PaddingValues(0.dp),
    /** reverse the direction of scrolling and layout */
    reverseLayout: Boolean = false,
    /** The layout orientation of the grid */
    isVertical: Boolean,
//    /** fling behavior to be used for flinging */
//    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    /** Whether scrolling via the user gestures is allowed. */
    userScrollEnabled: Boolean,
    /** The vertical arrangement for items/lines. */
    verticalArrangement: Arrangement.Vertical,
    /** The horizontal arrangement for items/lines. */
    horizontalArrangement: Arrangement.Horizontal,
    /** The number of lines to layout and display beyond the visible area */
    beyondBoundsLineCount: Int = 0,
    /** The content of the grid */
    content: LazyGridScope.() -> Unit
) {
    val itemProviderLambda = rememberLazyGridItemProviderLambda(state, content)

//    val semanticState = rememberLazyGridSemanticState(state, reverseLayout)

    val coroutineScope = rememberCoroutineScope()
    state.kuiklyInfo.scope = coroutineScope
//    val graphicsContext = LocalGraphicsContext.current
    val measurePolicy = rememberLazyGridMeasurePolicy(
        itemProviderLambda,
        state,
        slots,
        contentPadding,
        reverseLayout,
        isVertical,
        horizontalArrangement,
        verticalArrangement,
        coroutineScope,
        beyondBoundsLineCount
//        graphicsContext
    )

    val orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal
    state.contentPadding = contentPadding
    LazyLayout(
        modifier = modifier
            .then(state.remeasurementModifier)
            .then(state.awaitLayoutModifier)
//            .lazyLayoutSemantics(
//                itemProviderLambda = itemProviderLambda,
//                state = semanticState,
//                orientation = orientation,
//                userScrollEnabled = userScrollEnabled,
//                reverseScrolling = reverseLayout,
//            )
            .lazyLayoutBeyondBoundsModifier(
                state = rememberLazyGridBeyondBoundsState(state = state),
                beyondBoundsInfo = state.beyondBoundsInfo,
                reverseLayout = reverseLayout,
                layoutDirection = LocalLayoutDirection.current,
                orientation = orientation,
                enabled = userScrollEnabled
            )
            .then(state.itemAnimator.modifier),
//            .scrollingContainer(
//                state = state,
//                orientation = orientation,
//                enabled = userScrollEnabled,
//                reverseScrolling = reverseLayout,
//                flingBehavior = flingBehavior,
//                interactionSource = state.internalInteractionSource
//            ),
//        prefetchState = state.prefetchState,
        measurePolicy = measurePolicy,
        orientation = orientation,
        scrollableState = state,
        userScrollEnabled = userScrollEnabled,
        itemProvider = itemProviderLambda
    )
}

/** lazy grid slots configuration */
internal class LazyGridSlots(
    val sizes: IntArray,
    val positions: IntArray
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberLazyGridMeasurePolicy(
    /** Items provider of the list. */
    itemProviderLambda: () -> LazyGridItemProvider,
    /** The state of the list. */
    state: LazyGridState,
    /** Prefix sums of cross axis sizes of slots of the grid. */
    slots: LazyGridSlotsProvider,
    /** The inner padding to be added for the whole content(nor for each individual item) */
    contentPadding: PaddingValues,
    /** reverse the direction of scrolling and layout */
    reverseLayout: Boolean,
    /** The layout orientation of the list */
    isVertical: Boolean,
    /** The horizontal arrangement for items */
    horizontalArrangement: Arrangement.Horizontal?,
    /** The vertical arrangement for items */
    verticalArrangement: Arrangement.Vertical?,
    /** Coroutine scope for item animations */
    coroutineScope: CoroutineScope,
    /** The number of lines to layout and display beyond the visible area */
    beyondBoundsLineCount: Int = 0
//    /** Used for creating graphics layers */
//    graphicsContext: GraphicsContext
) = remember<LazyLayoutMeasureScope.(Constraints) -> MeasureResult>(
    state,
    slots,
    contentPadding,
    reverseLayout,
    isVertical,
    horizontalArrangement,
    verticalArrangement,
    beyondBoundsLineCount
//    graphicsContext
) {
    { containerConstraints ->
        state.measurementScopeInvalidator.attachToScope()
        checkScrollableContainerConstraints(
            containerConstraints,
            if (isVertical) Orientation.Vertical else Orientation.Horizontal
        )

        // resolve content paddings
        val startPadding =
            if (isVertical) {
                contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
            } else {
                // in horizontal configuration, padding is reversed by placeRelative
                contentPadding.calculateStartPadding(layoutDirection).roundToPx()
            }

        val endPadding =
            if (isVertical) {
                contentPadding.calculateRightPadding(layoutDirection).roundToPx()
            } else {
                // in horizontal configuration, padding is reversed by placeRelative
                contentPadding.calculateEndPadding(layoutDirection).roundToPx()
            }
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val bottomPadding = contentPadding.calculateBottomPadding().roundToPx()
        val totalVerticalPadding = topPadding + bottomPadding
        val totalHorizontalPadding = startPadding + endPadding
        val totalMainAxisPadding = if (isVertical) totalVerticalPadding else totalHorizontalPadding
        val beforeContentPadding = when {
            isVertical && !reverseLayout -> topPadding
            isVertical && reverseLayout -> bottomPadding
            !isVertical && !reverseLayout -> startPadding
            else -> endPadding // !isVertical && reverseLayout
        }
        val afterContentPadding = totalMainAxisPadding - beforeContentPadding
        val contentConstraints =
            containerConstraints.offset(-totalHorizontalPadding, -totalVerticalPadding)

        val itemProvider = itemProviderLambda()
        val spanLayoutProvider = itemProvider.spanLayoutProvider
        val resolvedSlots = slots.invoke(density = this, constraints = containerConstraints)
        val slotsPerLine = resolvedSlots.sizes.size
        spanLayoutProvider.slotsPerLine = slotsPerLine

        val spaceBetweenLinesDp = if (isVertical) {
            requireNotNull(verticalArrangement) {
                "null verticalArrangement when isVertical == true"
            }.spacing
        } else {
            requireNotNull(horizontalArrangement) {
                "null horizontalArrangement when isVertical == false"
            }.spacing
        }
        val spaceBetweenLines = spaceBetweenLinesDp.roundToPx()
        val itemsCount = itemProvider.itemCount

        if (state.kuiklyInfo.cachedTotalItems > 0 && itemsCount < state.kuiklyInfo.cachedTotalItems) {
            state.kuiklyInfo.offsetDirty = true
        }
        state.kuiklyInfo.cachedTotalItems = itemsCount

        // can be negative if the content padding is larger than the max size from constraints
        val mainAxisAvailableSize = if (isVertical) {
            containerConstraints.maxHeight - totalVerticalPadding
        } else {
            containerConstraints.maxWidth - totalHorizontalPadding
        }
        val visualItemOffset = if (!reverseLayout || mainAxisAvailableSize > 0) {
            IntOffset(startPadding, topPadding)
        } else {
            // When layout is reversed and paddings together take >100% of the available space,
            // layout size is coerced to 0 when positioning. To take that space into account,
            // we offset start padding by negative space between paddings.
            IntOffset(
                if (isVertical) startPadding else startPadding + mainAxisAvailableSize,
                if (isVertical) topPadding + mainAxisAvailableSize else topPadding
            )
        }

        val measuredItemProvider = object : LazyGridMeasuredItemProvider(
            itemProvider,
            this,
            spaceBetweenLines
        ) {
            override fun createItem(
                index: Int,
                key: Any,
                contentType: Any?,
                crossAxisSize: Int,
                mainAxisSpacing: Int,
                placeables: List<Placeable>,
                constraints: Constraints,
                lane: Int,
                span: Int
            ) = LazyGridMeasuredItem(
                index = index,
                key = key,
                isVertical = isVertical,
                crossAxisSize = crossAxisSize,
                mainAxisSpacing = mainAxisSpacing,
                reverseLayout = reverseLayout,
                layoutDirection = layoutDirection,
                beforeContentPadding = beforeContentPadding,
                afterContentPadding = afterContentPadding,
                visualOffset = visualItemOffset,
                placeables = placeables,
                contentType = contentType,
                animator = state.itemAnimator,
                constraints = constraints,
                lane = lane,
                span = span
            )
        }
        val measuredLineProvider = object : LazyGridMeasuredLineProvider(
            isVertical = isVertical,
            slots = resolvedSlots,
            gridItemsCount = itemsCount,
            spaceBetweenLines = spaceBetweenLines,
            measuredItemProvider = measuredItemProvider,
            _spanLayoutProvider = spanLayoutProvider
        ) {
            override fun createLine(
                index: Int,
                items: Array<LazyGridMeasuredItem>,
                spans: List<GridItemSpan>,
                mainAxisSpacing: Int
            ): LazyGridMeasuredLine {
                val lineResult = LazyGridMeasuredLine(
                    index = index,
                    items = items,
                    spans = spans,
                    slots = resolvedSlots,
                    isVertical = isVertical,
                    mainAxisSpacing = mainAxisSpacing,
                )

                // 检查行高度是否扩大了 - 基于行的第一个item的key作为行的唯一标识
                val lineKey = "line_${index}_${items.firstOrNull()?.key ?: ""}"
                val oldLineHeight = state.kuiklyInfo.itemMainSpaceCache[lineKey]
                // 行高度扩大了
                if ((oldLineHeight ?: 0) < lineResult.mainAxisSizeWithSpacings && !state.isScrollInProgress) {
                    state.kuiklyInfo.realContentSize = null
                    state.tryExpandStartSizeNoScroll()
                }
                state.kuiklyInfo.itemMainSpaceCache[lineKey] = lineResult.mainAxisSizeWithSpacings

                return lineResult
            }
        }
        val prefetchInfoRetriever: (line: Int) -> List<Pair<Int, Constraints>> = { line ->
            val lineConfiguration = spanLayoutProvider.getLineConfiguration(line)
            var index = lineConfiguration.firstItemIndex
            var slot = 0
            val result = ArrayList<Pair<Int, Constraints>>(lineConfiguration.spans.size)
            lineConfiguration.spans.fastForEach {
                val span = it.currentLineSpan
                result.add(index to measuredLineProvider.childConstraints(slot, span))
                ++index
                slot += span
            }
            result
        }

        val firstVisibleLineIndex: Int
        val firstVisibleLineScrollOffset: Int

        Snapshot.withoutReadObservation {
            val index = state.updateScrollPositionIfTheFirstItemWasMoved(
                itemProvider, state.firstVisibleItemIndex
            )
            if (index < itemsCount || itemsCount <= 0) {
                firstVisibleLineIndex = spanLayoutProvider.getLineIndexOfItem(index)
                firstVisibleLineScrollOffset = state.firstVisibleItemScrollOffset
            } else {
                // the data set has been updated and now we have less items that we were
                // scrolled to before
                firstVisibleLineIndex = spanLayoutProvider.getLineIndexOfItem(itemsCount - 1)
                firstVisibleLineScrollOffset = 0
            }
        }

        val pinnedItems = itemProvider.calculateLazyLayoutPinnedIndices(
            state.pinnedItems,
            state.beyondBoundsInfo
        )

        // todo: wrap with snapshot when b/341782245 is resolved
        val measureResult =
            measureLazyGrid(
                itemsCount = itemsCount,
                measuredLineProvider = measuredLineProvider,
                measuredItemProvider = measuredItemProvider,
                mainAxisAvailableSize = mainAxisAvailableSize,
                beforeContentPadding = beforeContentPadding,
                afterContentPadding = afterContentPadding,
                spaceBetweenLines = spaceBetweenLines,
                firstVisibleLineIndex = firstVisibleLineIndex,
                firstVisibleLineScrollOffset = firstVisibleLineScrollOffset,
                scrollToBeConsumed = state.scrollToBeConsumed,
                constraints = contentConstraints,
                isVertical = isVertical,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                reverseLayout = reverseLayout,
                density = this,
                itemAnimator = state.itemAnimator,
                slotsPerLine = slotsPerLine,
                pinnedItems = pinnedItems,
                beyondBoundsLineCount = beyondBoundsLineCount,
                coroutineScope = coroutineScope,
                placementScopeInvalidator = state.placementScopeInvalidator,
                prefetchInfoRetriever = prefetchInfoRetriever,
//                graphicsContext = graphicsContext,
                layout = { width, height, placement ->
                    layout(
                        containerConstraints.constrainWidth(width + totalHorizontalPadding),
                        containerConstraints.constrainHeight(height + totalVerticalPadding),
                        emptyMap(),
                        placement
                    )
                }
            )
        state.applyMeasureResult(measureResult)
        measureResult
    }
}
