/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.kuikly.core.render.android.expand.component.text

import android.graphics.Canvas
import android.text.Layout

/**
 * 富文本绘制器，封装 [Layout]，用于富文本视图的测量与绘制。
 */
internal class KRRichTextViewDrawer(val textLayout: Layout) {
    internal var selectionStart = -1
    internal var selectionEnd = -1
    internal val hasSelection: Boolean get() = 0 <= selectionStart && selectionStart < selectionEnd

    /**
     * 将文本内容绘制到 [canvas]，对接到 [Layout.draw]。
     */
    fun draw(canvas: Canvas) {
        textLayout.draw(canvas)
    }
}