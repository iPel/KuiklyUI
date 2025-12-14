package com.tencent.kuikly.core.render.android.expand.component

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Magnifier
import androidx.annotation.RequiresApi
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderLog
import com.tencent.kuikly.core.render.android.css.ktx.toDpF
import com.tencent.kuikly.core.render.android.css.ktx.toPxF
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import org.json.JSONObject

private const val DEBUG_LOG = true

private const val SELECTABLE_INHERIT = 0
private const val SELECTABLE_ENABLE = 1
private const val SELECTABLE_DISABLE = 2

private const val SELECTION_TYPE_CHARACTER = 0
private const val SELECTION_TYPE_WORD = 1
private const val SELECTION_TYPE_PARAGRAPH = 2
private const val SELECTION_TYPE_SENTENCE = 3

private const val CURSOR_WIDTH = 10f
private const val CURSOR_HEIGHT = 25f
private const val CURSOR_RADIUS = 6f
private const val CURSOR_HIT_EXTRA = 10f
private const val DRAG_DESCENT = 2f

private inline fun logInfo(msg: () -> String) {
    if (DEBUG_LOG) {
        KuiklyRenderLog.i("KRTextSelector", msg())
    }
}

internal class KRTextSelector(private val view: KRView) {

    companion object {

        /**
         * Recursively find the parent text selector
         */
        fun KRRichTextView.findParentTextSelector(): KRTextSelector? {
            // FIXME: KRRichTextView is unexpectedly a KRView, should we check self first?
            this.textSelector?.also {
                return it
            }
            var parentView = this.parent
            while (parentView != null) {
                if (parentView is KRView) {
                    parentView.textSelector?.also {
                        return it
                    }
                }
                parentView = parentView.parent
            }
            return null
        }

        private fun KRTextSelector.forEachText(
            action: KRRichTextView.(offsetX: Int, offsetY: Int) -> Unit
        ) {
            assert(selectable != SELECTABLE_DISABLE)
            // FIXME: KRRichTextView is unexpectedly a KRView, should we check self first?
            if (view is KRRichTextView) {
                view.action(0, 0)
                return
            }
            fun forEachTextInner(parent: ViewGroup, parentOffsetX: Int, parentOffsetY: Int) {
                assert(parent !is KRRichTextView)
                for (i in 0 until parent.childCount) {
                    val child = parent.getChildAt(i)
                    if (child is KRView && child.textSelector?.selectable == SELECTABLE_DISABLE) {
                        continue
                    }
                    if (child is KRRichTextView) {
                        // must check first, cus KRRichTextView is ViewGroup
                        child.action(parentOffsetX + child.left, parentOffsetY + child.top)
                    } else if (child is ViewGroup) {
                        forEachTextInner(
                            child,
                            parentOffsetX + child.left,
                            parentOffsetY + child.top
                        )
                    }
                }
            }
            forEachTextInner(view, 0, 0)
        }

    }

    private inline val kuiklyContext get() = view.kuiklyRenderContext

    private val reusableTextViewList = ArrayList<Triple<KRRichTextView, Int, Int>>()
    private val reusableRect = RectF()
    private val reusablePath = Path()

    private val selectionPaint = Paint().also {
        it.style = Paint.Style.FILL
        it.color = 0x6633B5E5 // default to translucent blue
    }
    private val cursorPaint = Paint().also {
        it.style = Paint.Style.FILL
        it.color = 0xFF33B5E5.toInt() // default to blue
    }

    private var selectable = SELECTABLE_INHERIT
    private inline val enabled get() = selectable == SELECTABLE_ENABLE
    private var cursorStartX: Float = Float.NaN
    private var cursorStartY: Float = Float.NaN
    private var cursorEndX: Float = Float.NaN
    private var cursorEndY: Float = Float.NaN
    private var touchDownX: Float = Float.NaN
    private var touchDownY: Float = Float.NaN

    private var selectStartCallback: KuiklyRenderCallback? = null
    private var selectChangeCallback: KuiklyRenderCallback? = null
    private var selectEndCallback: KuiklyRenderCallback? = null
    private var selectCancelCallback: KuiklyRenderCallback? = null

    /**
     * active selecting mode
     */
    private var active: Boolean = false
    private var selectionRect = RectF()

    // dragging variables
    private var dragging = false
    private var dragFixedX: Float = Float.NaN
    private var dragFixedY: Float = Float.NaN
    private var dragMovableX: Float = Float.NaN
    private var dragMovableY: Float = Float.NaN

    // magnifier support
    private val magnifierAnimator by lazy(LazyThreadSafetyMode.NONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            MagnifierMotionAnimator(view)
        } else {
            null
        }
    }

    private val updateMagnifierRunnable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Runnable { magnifierAnimator!!.update() }
    } else {
        null
    }

    init {
        forEachText { _, _ -> parentTextSelector = this@KRTextSelector }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enabled) {
            return false
        }
        val handled = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(event.x, event.y)
            MotionEvent.ACTION_MOVE -> handleTouchMove(event.x, event.y)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> handleTouchUp()
            else -> dragging
        }
        return handled
    }

    private fun handleTouchDown(x: Float, y: Float): Boolean {
        touchDownX = x
        touchDownY = y
        if (active) {
            val cursorWidth = kuiklyContext.toPxF(CURSOR_WIDTH)
            val cursorHeight = kuiklyContext.toPxF(CURSOR_HEIGHT)
            val cursorHitExtra = kuiklyContext.toPxF(CURSOR_HIT_EXTRA)
            // used to adjust y into the text line
            val dragDescent = kuiklyContext.toPxF(DRAG_DESCENT)
            if (x >= cursorEndX - cursorHitExtra &&
                x <= cursorEndX + cursorWidth + cursorHitExtra &&
                y >= cursorEndY - cursorHitExtra &&
                y <= cursorEndY + cursorHeight + cursorHitExtra
            ) {
                // hit end cursor
                dragging = true
                dragFixedX = cursorStartX
                dragFixedY = cursorStartY - dragDescent
                dragMovableX = touchDownX
                dragMovableY = cursorEndY - dragDescent
                return true
            } else if (x >= cursorStartX - cursorWidth - cursorHitExtra &&
                x <= cursorStartX + cursorHitExtra &&
                y >= cursorStartY - cursorHitExtra &&
                y <= cursorStartY + cursorHeight + cursorHitExtra
            ) {
                // hit start cursor
                dragging = true
                dragFixedX = cursorEndX
                dragFixedY = cursorEndY - dragDescent
                dragMovableX = touchDownX
                dragMovableY = cursorStartY - dragDescent
                return true
            }
        }
        return false
    }

    private fun handleTouchMove(x: Float, y: Float): Boolean {
        if (!dragging) {
            return false
        }
        val deltaX = x - touchDownX
        val deltaY = y - touchDownY

        val x1 = dragFixedX
        val y1 = dragFixedY
        val x2 = dragMovableX + deltaX
        val y2 = dragMovableY + deltaY

        updateSelectionByCoordinate(x1, y1, x2, y2)
        magnifierAnimator?.show(x, if (draggingCursorStart()) cursorStartY else cursorEndY)
        return true
    }

    private fun handleTouchUp(): Boolean {
        if (!dragging) {
            return false
        }
        dragging = false
        dragFixedX = Float.NaN
        dragFixedY = Float.NaN
        dragMovableX = Float.NaN
        dragMovableY = Float.NaN
        touchDownX = Float.NaN
        touchDownY = Float.NaN
        magnifierAnimator?.dismiss()
        notifySelectEnd()
        return true
    }

    fun performLongClick(): Boolean {
        // todo support long click
        return false
    }

    fun performBackPressed(): Boolean {
        if (active) {
            clearSelection()
            return true
        }
        return false
    }

    fun destroy() {
        logInfo { "destroy" }
        if (selectable != SELECTABLE_DISABLE) {
            forEachText { _, _ ->
                clearSelection()
                parentTextSelector = null
            }
        }
    }

    fun setSelectable(option: Int) {
        if (selectable == option) {
            return
        }
        selectable = option
        view.isLongClickable = enabled
        if (!enabled) {
            clearSelection()
        }
    }

    fun setSelectionColor(option: JSONObject) {
        val background = option.optLong("background", -1L)
        val cursor = option.optLong("cursor", -1L)
        if (background != -1L && cursor != -1L) {
            cursorPaint.color = cursor.toInt()
            selectionPaint.color = background.toInt()
        }
    }

    fun setSelectStartCallback(callback: KuiklyRenderCallback) {
        selectStartCallback = callback
    }

    fun setSelectChangeCallback(callback: KuiklyRenderCallback) {
        selectChangeCallback = callback
    }

    fun setSelectEndCallback(callback: KuiklyRenderCallback) {
        selectEndCallback = callback
    }

    fun setSelectCancelCallback(callback: KuiklyRenderCallback) {
        selectCancelCallback = callback
    }

    fun createSelection(option: JSONObject) {
        logInfo { "createSelection: $option" }
        val wasActive = active
        if (active) {
            forEachText { _, _ ->
                clearSelection()
            }
        }
        val x = kuiklyContext.toPxF(option.optDouble("x").toFloat())
        val y = kuiklyContext.toPxF(option.optDouble("y").toFloat())
        val type = when (option.optInt("type")) {
            SELECTION_TYPE_CHARACTER -> SelectionType.CHARACTER
            SELECTION_TYPE_WORD -> SelectionType.WORD
            SELECTION_TYPE_PARAGRAPH -> SelectionType.PARAGRAPH
            SELECTION_TYPE_SENTENCE -> SelectionType.SENTENCE
            else -> SelectionType.WORD
        }
        reusableTextViewList.clear()
        forEachText { offsetX, offsetY ->
            // 判断相交
            if (x < offsetX + this.width && x > offsetX &&
                y < offsetY + this.height && y > offsetY &&
                this.visibility == View.VISIBLE
            ) {
                reusableTextViewList.add(Triple(this, offsetX, offsetY))
            }
        }
        logInfo { "collect size=${reusableTextViewList.size}" }
        // sort by position, top to bottom, left to right
        reusableTextViewList.sortWith(
            Comparator { (_, x1, y1), (_, x2, y2) -> if (y1 == y2) x1 - x2 else y1 - y2 }
        )
        for (i in reusableTextViewList.size - 1 downTo 0) {
            val (textView, offsetX, offsetY) = reusableTextViewList[i]
            if (textView.setSelectionByCoordinate(x - offsetX, y - offsetY, type)) {
                textView.getSelectionStartPosition().also { (x, y) ->
                    cursorStartX = x + offsetX
                    cursorStartY = y + offsetY
                }
                textView.getSelectionEndPosition().also { (x, y) ->
                    cursorEndX = x + offsetX
                    cursorEndY = y + offsetY
                }
                logInfo { "position hit i=$i view hash=${textView.hashCode()}" }
                textView.getSelectionRect(reusableRect)
                selectionRect.left = reusableRect.left + offsetX
                selectionRect.top = reusableRect.top + offsetY
                selectionRect.right = reusableRect.right + offsetX
                selectionRect.bottom = reusableRect.bottom + offsetY
                if (!wasActive) {
                    notifySelectStart()
                } else {
                    notifySelectChange()
                }
                break
            }
        }
        if (wasActive && !active) {
            clearSelection()
        }
    }

    fun selectAll() {
        logInfo { "select all active=$active" }
        val wasActive = active
        val width = view.width.toFloat()
        val height = view.height.toFloat()

        var firstX = Int.MAX_VALUE
        var firstY = Int.MAX_VALUE
        var firstView: KRRichTextView? = null
        var lastX: Int = Int.MIN_VALUE
        var lastY: Int = Int.MIN_VALUE
        var lastView: KRRichTextView? = null

        var selectionLeft = Float.MAX_VALUE
        var selectionTop = Float.MAX_VALUE
        var selectionRight = Float.MIN_VALUE
        var selectionBottom = Float.MIN_VALUE

        forEachText { offsetX, offsetY ->
            // 判断相交
            if (this.visibility == View.VISIBLE) {
                val hit = updateSelectionByCoordinate(
                    -offsetX.toFloat(),
                    -offsetY.toFloat(),
                    width - offsetX,
                    height - offsetY
                )
                if (hit) {
                    if (offsetY < firstY || (offsetY == firstY && offsetX < firstX)) {
                        firstX = offsetX
                        firstY = offsetY
                        firstView = this
                    }
                    if (offsetY > lastY || (offsetY == lastY && offsetX > lastX)) {
                        lastX = offsetX
                        lastY = offsetY
                        lastView = this
                    }
                    getSelectionRect(reusableRect)
                    selectionLeft = minOf(selectionLeft, reusableRect.left + offsetX)
                    selectionTop = minOf(selectionTop, reusableRect.top + offsetY)
                    selectionRight = maxOf(selectionRight, reusableRect.right + offsetX)
                    selectionBottom = maxOf(selectionBottom, reusableRect.bottom + offsetY)
                }
            }
        }
        if (firstView != null && lastView != null) {
            firstView!!.getSelectionStartPosition().also { (x, y) ->
                cursorStartX = x + firstX
                cursorStartY = y + firstY
            }
            lastView!!.getSelectionEndPosition().also { (x, y) ->
                cursorEndX = x + lastX
                cursorEndY = y + lastY
            }
            selectionRect.left = selectionLeft
            selectionRect.top = selectionTop
            selectionRect.right = selectionRight
            selectionRect.bottom = selectionBottom
            if (!wasActive) {
                notifySelectStart()
            } else {
                notifySelectChange()
            }
        }
    }

    private fun updateSelectionByCoordinate(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ) {
        logInfo { "updateSelectionByCoordinate: ($startX,$startY)-($endX,$endY)" }
        reusableTextViewList.clear()
        val minX = minOf(startX, endX)
        val minY = minOf(startY, endY)
        val maxX = maxOf(startX, endX)
        val maxY = maxOf(startY, endY)
        forEachText { offsetX, offsetY ->
            // 判断相交
            if (minX < offsetX + this.width && maxX > offsetX &&
                minY < offsetY + this.height && maxY > offsetY &&
                this.visibility == View.VISIBLE
            ) {
                reusableTextViewList.add(Triple(this, offsetX, offsetY))
            } else {
                clearSelection()
            }
        }
        logInfo { "collect size=${reusableTextViewList.size}" }
        // sort by position, top to bottom, left to right
        reusableTextViewList.sortWith(
            Comparator { (_, x1, y1), (_, x2, y2) -> if (y1 == y2) x1 - x2 else y1 - y2 }
        )

        val oldCursorStartX = cursorStartX
        val oldCursorStartY = cursorStartY
        val oldCursorEndX = cursorEndX
        val oldCursorEndY = cursorEndY
        var foundStart = false
        var foundEnd = false
        var selectionLeft = Float.MAX_VALUE
        var selectionTop = Float.MAX_VALUE
        var selectionRight = Float.MIN_VALUE
        var selectionBottom = Float.MIN_VALUE
        for (i in 0 until reusableTextViewList.size) {
            val (textView, offsetX, offsetY) = reusableTextViewList[i]
            if (foundEnd) {
                textView.clearSelection()
                continue
            }
            val hit = textView.updateSelectionByCoordinate(
                startX - offsetX,
                startY - offsetY,
                endX - offsetX,
                endY - offsetY
            )
            if (hit) {
                textView.getSelectionRect(reusableRect)
                selectionLeft = minOf(selectionLeft, reusableRect.left + offsetX)
                selectionTop = minOf(selectionTop, reusableRect.top + offsetY)
                selectionRight = maxOf(selectionRight, reusableRect.right + offsetX)
                selectionBottom = maxOf(selectionBottom, reusableRect.bottom + offsetY)
            }
            if (!foundStart && hit) {
                logInfo { "start hit i=$i view hash=${textView.hashCode()}" }
                foundStart = true
                textView.getSelectionStartPosition().also { (x, y) ->
                    cursorStartX = x + offsetX
                    cursorStartY = y + offsetY
                }
            } else if (foundStart && !hit) {
                logInfo { "end hit i=$i view hash=${textView.hashCode()}" }
                foundEnd = true
                val (endTextView, endOffsetX, endOffsetY) = reusableTextViewList[i - 1]
                endTextView.getSelectionEndPosition().also { (x, y) ->
                    cursorEndX = x + endOffsetX
                    cursorEndY = y + endOffsetY
                }
            }
        }
        if (!foundStart) {
            // restore old position
            val dragDescent = kuiklyContext.toPxF(DRAG_DESCENT)
            restoreSelection(
                oldCursorStartX,
                oldCursorStartY - dragDescent,
                oldCursorEndX,
                oldCursorEndY - dragDescent
            )
        } else if (!foundEnd) {
            // update end position to last
            val (textView, offsetX, offsetY) = reusableTextViewList.last()
            textView.getSelectionEndPosition().also { (x, y) ->
                cursorEndX = x + offsetX
                cursorEndY = y + offsetY
            }
        }
        if (oldCursorStartX != cursorStartX || oldCursorStartY != cursorStartY ||
            oldCursorEndX != cursorEndX || oldCursorEndY != cursorEndY
        ) {
            selectionRect.left = selectionLeft
            selectionRect.top = selectionTop
            selectionRect.right = selectionRight
            selectionRect.bottom = selectionBottom
            notifySelectChange()
        }
    }

    private fun restoreSelection(startX: Float, startY: Float, endX: Float, endY: Float) {
        reusableTextViewList.clear()
        val minX = minOf(startX, endX)
        val minY = minOf(startY, endY)
        val maxX = maxOf(startX, endX)
        val maxY = maxOf(startY, endY)
        forEachText { offsetX, offsetY ->
            // 判断相交
            if (minX < offsetX + this.width && maxX > offsetX &&
                minY < offsetY + this.height && maxY > offsetY &&
                this.visibility == View.VISIBLE
            ) {
                reusableTextViewList.add(Triple(this, offsetX, offsetY))
            }
        }
        if (reusableTextViewList.isEmpty()) {
            // this should never happen
            logInfo { "restore selection failed" }
            // TODO clean up
            return
        }
        // sort by position, top to bottom, left to right
        reusableTextViewList.sortWith(
            Comparator { (_, x1, y1), (_, x2, y2) -> if (y1 == y2) x1 - x2 else y1 - y2 }
        )
        for (i in reusableTextViewList.size - 1 downTo 0) {
            val (textView, offsetX, offsetY) = reusableTextViewList[i]
            val hit = textView.updateSelectionByCoordinate(
                startX - offsetX,
                startY - offsetY,
                endX - offsetX,
                endY - offsetY,
                i == 0
            )
            if (hit) {
                logInfo { "restore selection hit i=$i view hash=${textView.hashCode()}" }
                return
            }
        }
        // this should never happen too
        logInfo { "restore selection hit failed" }
        // TODO clean up
    }

    private fun generateSelectEventParam() = mapOf(
        "x" to kuiklyContext.toDpF(selectionRect.left),
        "y" to kuiklyContext.toDpF(selectionRect.top),
        "width" to kuiklyContext.toDpF(selectionRect.width()),
        "height" to kuiklyContext.toDpF(selectionRect.height())
    )

    private fun notifySelectStart() {
        active = true
        view.isFocusableInTouchMode = true
        view.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                clearSelection()
            }
        }
        selectStartCallback?.invoke(generateSelectEventParam())
        view.invalidate()
        view.requestFocus()
    }

    private fun notifySelectChange() {
        selectChangeCallback?.invoke(generateSelectEventParam())
        view.invalidate()
    }

    private fun notifySelectEnd() {
        selectEndCallback?.invoke(generateSelectEventParam())
        view.invalidate()
    }

    private fun notifySelectCancel() {
        view.onFocusChangeListener = null
        view.isFocusable = false
        view.clearFocus()
        active = false
        selectCancelCallback?.invoke(null)
        view.invalidate()
    }

    fun getSelection(callback: KuiklyRenderCallback) {
        val result = mutableListOf<Triple<String, Int, Int>>()
        forEachText { offsetX, offsetY ->
            if (this.visibility != View.VISIBLE) {
                return@forEachText
            }
            val selectedText = getSelectionText()
            if (selectedText.isNotEmpty()) {
                result.add(Triple(selectedText, offsetX, offsetY))
            }
        }
        result.sortWith(Comparator { (_, x1, y1), (_, x2, y2) -> if (y1 == y2) x1 - x2 else y1 - y2 })
        callback(mapOf("content" to result.map { (text) -> text }))
    }

    fun clearSelection() {
        logInfo { "clearSelection" }
        if (active) {
            forEachText { _, _ ->
                clearSelection()
            }
            notifySelectCancel()
        }
    }

    fun drawSelection(textView: KRRichTextView, canvas: Canvas) {
        if (!active) {
            return
        }
        if (textView.getSelectionPath(reusablePath)) {
            canvas.drawPath(reusablePath, selectionPaint)
        }
    }

    fun drawCursor(canvas: Canvas) {
        if (!active) {
            return
        }
        // Cursor dimensions
        val cursorWidth = kuiklyContext.toPxF(CURSOR_WIDTH)
        val cursorHeight = kuiklyContext.toPxF(CURSOR_HEIGHT)
        val cursorRadius = kuiklyContext.toPxF(CURSOR_RADIUS)

        reusablePath.apply {
            rewind()
            if (!draggingCursorStart()) {
                moveTo(cursorStartX, cursorStartY)
                rLineTo(-cursorWidth, cursorWidth)
                rLineTo(0f, cursorHeight - cursorWidth - cursorRadius)
                arcTo(
                    cursorStartX - cursorWidth,
                    cursorStartY + cursorHeight - cursorRadius * 2,
                    cursorStartX - cursorWidth + cursorRadius * 2,
                    cursorStartY + cursorHeight,
                    180f,
                    -90f,
                    false
                )
                lineTo(cursorStartX, cursorStartY + cursorHeight)
                close()
            }

            if (!draggingCursorEnd()) {
                moveTo(cursorEndX, cursorEndY)
                rLineTo(cursorWidth, cursorWidth)
                rLineTo(0f, cursorHeight - cursorWidth - cursorRadius)
                arcTo(
                    cursorEndX + cursorWidth - cursorRadius * 2,
                    cursorEndY + cursorHeight - cursorRadius * 2,
                    cursorEndX + cursorWidth,
                    cursorEndY + cursorHeight,
                    0f,
                    90f,
                    false
                )
                lineTo(cursorEndX, cursorEndY + cursorHeight)
                close()
            }
        }
        canvas.drawPath(reusablePath, cursorPaint)
    }

    private fun draggingCursorStart(): Boolean {
        if (dragging) {
            val dragDescent = kuiklyContext.toPxF(DRAG_DESCENT)
            // end cursor at fixed position or even above (means trimmed for line end)
            return dragFixedY + dragDescent > cursorEndY ||
                    (dragFixedY + dragDescent == cursorEndY && dragFixedX == cursorEndX)
        }
        return false
    }

    private fun draggingCursorEnd(): Boolean {
        if (dragging) {
            val dragDescent = kuiklyContext.toPxF(DRAG_DESCENT)
            // start cursor at fixed position or even below (means trimmed for line start)
            return dragFixedY + dragDescent < cursorStartY ||
                    (dragFixedY + dragDescent == cursorStartY && dragFixedX == cursorStartX)
        }
        return false
    }

}

/**
 * A value animator used to animate the magnifier.
 * copy from Android TextView source code.
 */
private class MagnifierMotionAnimator
@RequiresApi(Build.VERSION_CODES.P) constructor(view: View) {
    // The magnifier being animated.
    private val mMagnifier: Magnifier

    // Prepare the animator used to run the motion animation.
    private val mAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

    // Whether the magnifier is currently visible.
    private var mMagnifierIsShowing = false

    // The coordinates of the magnifier when the currently running animation started.
    private var mAnimationStartX = 0f
    private var mAnimationStartY = 0f

    // The coordinates of the magnifier in the latest animation frame.
    private var mAnimationCurrentX = 0f
    private var mAnimationCurrentY = 0f

    // The latest coordinates the motion animator was asked to #show() the magnifier at.
    private var mLastX = 0f
    private var mLastY = 0f

    init {
        mMagnifier = Magnifier(view)
        mAnimator.setDuration(DURATION)
        mAnimator.interpolator = LinearInterpolator()
        mAnimator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animation: ValueAnimator? ->
            // Interpolate to find the current position of the magnifier.
            mAnimationCurrentX =
                (mAnimationStartX + (mLastX - mAnimationStartX) * animation!!.animatedFraction)
            mAnimationCurrentY =
                (mAnimationStartY + (mLastY - mAnimationStartY) * animation.animatedFraction)
            mMagnifier.show(mAnimationCurrentX, mAnimationCurrentY)
        })
    }

    /**
     * Shows the magnifier at a new position.
     * If the y coordinate is different from the previous y coordinate
     * (probably corresponding to a line jump in the text), a short
     * animation is added to the jump.
     */
    fun show(x: Float, y: Float) {
        val startNewAnimation = mMagnifierIsShowing && y != mLastY
        println("pel show magnifier x=$x y=$y startNewAnimation=$startNewAnimation")

        if (startNewAnimation) {
            if (mAnimator.isRunning()) {
                mAnimator.cancel()
                mAnimationStartX = mAnimationCurrentX
                mAnimationStartY = mAnimationCurrentY
            } else {
                mAnimationStartX = mLastX
                mAnimationStartY = mLastY
            }
            mAnimator.start()
        } else {
            if (!mAnimator.isRunning()) {
                @Suppress("NewApi")
                mMagnifier.show(x, y)
            }
        }
        mLastX = x
        mLastY = y
        mMagnifierIsShowing = true
    }

    /**
     * Updates the content of the magnifier.
     */
    fun update() {
        @Suppress("NewApi")
        mMagnifier.update()
    }

    /**
     * Dismisses the magnifier, or does nothing if it is already dismissed.
     */
    fun dismiss() {
        @Suppress("NewApi")
        mMagnifier.dismiss()
        mAnimator.cancel()
        mMagnifierIsShowing = false
    }

    companion object {
        private const val DURATION: Long = 100 /* miliseconds */
    }
}
