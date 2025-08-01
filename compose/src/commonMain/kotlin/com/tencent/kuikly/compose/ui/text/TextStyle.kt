/*
 * Copyright 2019 The Android Open Source Project
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

package com.tencent.kuikly.compose.ui.text

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.material3.tokens.TypeScaleTokens
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Shadow
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.text.style.TextDirection
import com.tencent.kuikly.compose.ui.text.style.TextForegroundStyle
import com.tencent.kuikly.compose.ui.text.style.TextIndent
import com.tencent.kuikly.compose.ui.unit.LayoutDirection
import com.tencent.kuikly.compose.ui.unit.TextUnit
import kotlin.jvm.JvmName

/**
 * Styling configuration for a `Text`.
 *
 * @sample com.tencent.kuikly.compose.ui.text.samples.TextStyleSample
 *
 * @param platformStyle Platform specific [TextStyle] parameters.
 *
 * @see AnnotatedString
 * @see SpanStyle
 * @see ParagraphStyle
 */
// Maintainer note: When adding a new constructor or copy parameter, make sure to add a test case to
// TextStyleInvalidationTest to ensure the correct phase(s) get invalidated.
@Immutable
class TextStyle internal constructor(
    internal val spanStyle: SpanStyle,
    internal val paragraphStyle: ParagraphStyle,
//    val platformStyle: PlatformTextStyle? = null,
) {

    /**
     * Styling configuration for a `Text`.
     *
     * @sample com.tencent.kuikly.compose.ui.text.samples.TextStyleSample
     *
     * @param color The text color.
     * @param fontSize The size of glyphs to use when painting the text. This
     * may be [TextUnit.Unspecified] for inheriting from another [TextStyle].
     * @param fontWeight The typeface thickness to use when painting the text (e.g., bold).
     * @param fontStyle The typeface variant to use when drawing the letters (e.g., italic).
     * @param fontSynthesis Whether to synthesize font weight and/or style when the requested weight
     * or style cannot be found in the provided font family.
     * @param fontFamily The font family to be used when rendering the text.
     * @param fontFeatureSettings The advanced typography settings provided by font. The format is
     * the same as the CSS font-feature-settings attribute:
     * https://www.w3.org/TR/css-fonts-3/#font-feature-settings-prop
     * @param letterSpacing The amount of space to add between each letter.
     * @param baselineShift The amount by which the text is shifted up from the current baseline.
     * @param textGeometricTransform The geometric transformation applied the text.
     * @param localeList The locale list used to select region-specific glyphs.
     * @param background The background color for the text.
     * @param textDecoration The decorations to paint on the text (e.g., an underline).
     * @param shadow The shadow effect applied on the text.
     * @param drawStyle Drawing style of text, whether fill in the text while drawing or stroke
     * around the edges.
     * @param textAlign The alignment of the text within the lines of the paragraph.
     * @param textDirection The algorithm to be used to resolve the final text and paragraph
     * direction: Left To Right or Right To Left. If no value is provided the system will use the
     * [LayoutDirection] as the primary signal.
     * @param lineHeight Line height for the [Paragraph] in [TextUnit] unit, e.g. SP or EM.
     * @param textIndent The indentation of the paragraph.
     * @param platformStyle Platform specific [TextStyle] parameters.
     * @param lineHeightStyle the configuration for line height such as vertical alignment of the
     * line, whether to apply additional space as a result of line height to top of first line top
     * and bottom of last line. The configuration is applied only when a [lineHeight] is defined.
     * When null, [LineHeightStyle.Default] is used.
     * @param lineBreak The line breaking configuration for the text.
     * @param hyphens The configuration of hyphenation.
     * @param textMotion Text character placement, whether to optimize for animated or static text.
     */
    constructor(
        color: Color = Color.Unspecified,
        fontSize: TextUnit = TextUnit.Unspecified,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null,
//        fontSynthesis: FontSynthesis? = null,
        fontFamily: FontFamily? = null,
//        fontFeatureSettings: String? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
//        baselineShift: BaselineShift? = null,
//        textGeometricTransform: TextGeometricTransform? = null,
//        localeList: LocaleList? = null,
        background: Color = Color.Unspecified,
        textDecoration: TextDecoration? = null,
        shadow: Shadow? = null,
//        drawStyle: DrawStyle? = null,
        textAlign: TextAlign = TextAlign.Left,
//        textDirection: TextDirection = TextDirection.Ltr,
        lineHeight: TextUnit = TextUnit.Unspecified,
        textIndent: TextIndent? = null,
//        platformStyle: PlatformTextStyle? = null,
//        lineHeightStyle: LineHeightStyle? = null,
//        lineBreak: LineBreak = LineBreak.Unspecified,
//        hyphens: Hyphens = Hyphens.Unspecified,
//        textMotion: TextMotion? = null,
    ) : this(
        SpanStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
//            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
//            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
//            baselineShift = baselineShift,
//            textGeometricTransform = textGeometricTransform,
//            localeList = localeList,
            background = background,
            textDecoration = textDecoration,
            shadow = shadow,
//            platformStyle = platformStyle?.spanStyle,
//            drawStyle = drawStyle
        ),
        ParagraphStyle(
            textAlign = textAlign,
//            textDirection = textDirection,
            lineHeight = lineHeight,
            textIndent = textIndent,
//            platformStyle = platformStyle?.paragraphStyle,
//            lineHeightStyle = lineHeightStyle,
//            lineBreak = lineBreak,
//            hyphens = hyphens,
//            textMotion = textMotion
        ),
//        platformStyle = platformStyle
    )

    /**
     * Styling configuration for a `Text`.
     *
     * @sample com.tencent.kuikly.compose.ui.text.samples.TextStyleBrushSample
     *
     * @param brush The brush to use when painting the text. If brush is given as null, it will be
     * treated as unspecified. It is equivalent to calling the alternative color constructor with
     * [Color.Unspecified]
     * @param alpha Opacity to be applied to [brush] from 0.0f to 1.0f representing fully
     * transparent to fully opaque respectively.
     * @param fontSize The size of glyphs to use when painting the text. This
     * may be [TextUnit.Unspecified] for inheriting from another [TextStyle].
     * @param fontWeight The typeface thickness to use when painting the text (e.g., bold).
     * @param fontStyle The typeface variant to use when drawing the letters (e.g., italic).
     * @param fontSynthesis Whether to synthesize font weight and/or style when the requested weight
     * or style cannot be found in the provided font family.
     * @param fontFamily The font family to be used when rendering the text.
     * @param fontFeatureSettings The advanced typography settings provided by font. The format is
     * the same as the CSS font-feature-settings attribute:
     * https://www.w3.org/TR/css-fonts-3/#font-feature-settings-prop
     * @param letterSpacing The amount of space to add between each letter.
     * @param baselineShift The amount by which the text is shifted up from the current baseline.
     * @param textGeometricTransform The geometric transformation applied the text.
     * @param localeList The locale list used to select region-specific glyphs.
     * @param background The background color for the text.
     * @param textDecoration The decorations to paint on the text (e.g., an underline).
     * @param shadow The shadow effect applied on the text.
     * @param drawStyle Drawing style of text, whether fill in the text while drawing or stroke
     * around the edges.
     * @param textAlign The alignment of the text within the lines of the paragraph.
     * @param textDirection The algorithm to be used to resolve the final text and paragraph
     * direction: Left To Right or Right To Left. If no value is provided the system will use the
     * [LayoutDirection] as the primary signal.
     * @param lineHeight Line height for the [Paragraph] in [TextUnit] unit, e.g. SP or EM.
     * @param textIndent The indentation of the paragraph.
     * @param platformStyle Platform specific [TextStyle] parameters.
     * @param lineHeightStyle the configuration for line height such as vertical alignment of the
     * line, whether to apply additional space as a result of line height to top of first line top
     * and bottom of last line. The configuration is applied only when a [lineHeight] is defined.
     * @param lineBreak The line breaking configuration for the text.
     * @param hyphens The configuration of hyphenation.
     * @param textMotion Text character placement, whether to optimize for animated or static text.
     */
    constructor(
        brush: Brush?,
        alpha: Float = Float.NaN,
        fontSize: TextUnit = TextUnit.Unspecified,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null,
//        fontSynthesis: FontSynthesis? = null,
        fontFamily: FontFamily? = null,
//        fontFeatureSettings: String? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
//        baselineShift: BaselineShift? = null,
//        textGeometricTransform: TextGeometricTransform? = null,
//        localeList: LocaleList? = null,
        background: Color = Color.Unspecified,
        textDecoration: TextDecoration? = null,
        shadow: Shadow? = null,
//        drawStyle: DrawStyle? = null,
        textAlign: TextAlign = TextAlign.Unspecified,
//        textDirection: TextDirection = TextDirection.Unspecified,
        lineHeight: TextUnit = TextUnit.Unspecified,
        textIndent: TextIndent? = null,
//        platformStyle: PlatformTextStyle? = null,
//        lineHeightStyle: LineHeightStyle? = null,
//        lineBreak: LineBreak = LineBreak.Unspecified,
//        hyphens: Hyphens = Hyphens.Unspecified,
//        textMotion: TextMotion? = null
    ) : this(
        SpanStyle(
            brush = brush,
            alpha = alpha,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
//            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
//            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
//            baselineShift = baselineShift,
//            textGeometricTransform = textGeometricTransform,
//            localeList = localeList,
            background = background,
            textDecoration = textDecoration,
            shadow = shadow,
//            platformStyle = platformStyle?.spanStyle,
//            drawStyle = drawStyle
        ),
        ParagraphStyle(
            textAlign = textAlign,
//            textDirection = textDirection,
            lineHeight = lineHeight,
            textIndent = textIndent,
//            platformStyle = platformStyle?.paragraphStyle,
//            lineHeightStyle = lineHeightStyle,
//            lineBreak = lineBreak,
//            hyphens = hyphens,
//            textMotion = textMotion
        ),
//        platformStyle = platformStyle
    )

    @Stable
    fun toSpanStyle(): SpanStyle = spanStyle

    @Stable
    fun toParagraphStyle(): ParagraphStyle = paragraphStyle

    /**
     * Returns a new text style that is a combination of this style and the given [other] style.
     *
     * [other] text style's null or inherit properties are replaced with the non-null properties of
     * this text style. Another way to think of it is that the "missing" properties of the [other]
     * style are _filled_ by the properties of this style.
     *
     * If the given text style is null, returns this text style.
     */
    @Stable
    fun merge(other: TextStyle? = null): TextStyle {
        if (other == null || other == Default) return this
        return TextStyle(
            spanStyle = toSpanStyle().merge(other.toSpanStyle()),
            paragraphStyle = toParagraphStyle().merge(other.toParagraphStyle())
        )
    }

    /**
     * Fast merge non-default values and parameters.
     *
     * This is the same algorithm as [merge] but does not require allocating it's parameter and may
     * return this instead of allocating a result when all values are default.
     *
     * This is a similar algorithm to [copy] but when either this or a parameter are set to a
     * default value, the other value will take precedent.
     *
     * To explain better, consider the following examples:
     *
     * Example 1:
     * - this.color = [Color.Unspecified]
     * - [color] = [Color.Red]
     * - result => [Color.Red]
     *
     * Example 2:
     * - this.color = [Color.Red]
     * - [color] = [Color.Unspecified]
     * - result => [Color.Red]
     *
     * Example 3:
     * - this.color = [Color.Red]
     * - [color] = [Color.Blue]
     * - result => [Color.Blue]
     *
     * You should _always_ use this method over the [merge]([TextStyle]) overload when you do not
     * already have a TextStyle allocated. You should chose this over [copy] when building a theming
     * system and applying styling information to a specific usage.
     *
     * @return this or a new TextLayoutResult with all parameters chosen to the non-default option
     * provided.
     *
     * @see merge
     */
    @Stable
    fun merge(
        color: Color = Color.Unspecified,
        fontSize: TextUnit = TextUnit.Unspecified,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null,
//        fontSynthesis: FontSynthesis? = null,
        fontFamily: FontFamily? = null,
//        fontFeatureSettings: String? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
//        baselineShift: BaselineShift? = null,
//        textGeometricTransform: TextGeometricTransform? = null,
//        localeList: LocaleList? = null,
        background: Color = Color.Unspecified,
        textDecoration: TextDecoration? = null,
        shadow: Shadow? = null,
//        drawStyle: DrawStyle? = null,
        textAlign: TextAlign = TextAlign.Unspecified,
//        textDirection: TextDirection = TextDirection.Unspecified,
        lineHeight: TextUnit = TextUnit.Unspecified,
        textIndent: TextIndent? = null,
//        lineHeightStyle: LineHeightStyle? = null,
//        lineBreak: LineBreak = LineBreak.Unspecified,
//        hyphens: Hyphens = Hyphens.Unspecified,
//        platformStyle: PlatformTextStyle? = null,
//        textMotion: TextMotion? = null
    ): TextStyle {
        val mergedSpanStyle: SpanStyle = spanStyle.fastMerge(
            color = color,
            brush = brush,
            alpha = Float.NaN,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
//            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
//            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
//            baselineShift = baselineShift,
//            textGeometricTransform = textGeometricTransform,
//            localeList = localeList,
            background = background,
            textDecoration = textDecoration,
            shadow = shadow,
//            platformStyle = platformStyle?.spanStyle,
//            drawStyle = drawStyle
        )
        val mergedParagraphStyle: ParagraphStyle = paragraphStyle.fastMerge(
            textAlign = textAlign,
//            textDirection = textDirection,
            lineHeight = lineHeight,
            textIndent = textIndent,
//            platformStyle = platformStyle?.paragraphStyle,
//            lineHeightStyle = lineHeightStyle,
//            lineBreak = lineBreak,
//            hyphens = hyphens,
//            textMotion = textMotion
        )
        if (spanStyle === mergedSpanStyle && paragraphStyle === mergedParagraphStyle) return this
        return TextStyle(mergedSpanStyle, mergedParagraphStyle)
    }

    /**
     * Returns a new text style that is a combination of this style and the given [other] style.
     *
     * @see merge
     */
    @Stable
    fun merge(other: SpanStyle): TextStyle {
        return TextStyle(
            spanStyle = toSpanStyle().merge(other),
            paragraphStyle = toParagraphStyle()
        )
    }

    /**
     * Returns a new text style that is a combination of this style and the given [other] style.
     *
     * @see merge
     */
    @Stable
    fun merge(other: ParagraphStyle): TextStyle {
        return TextStyle(
            spanStyle = toSpanStyle(),
            paragraphStyle = toParagraphStyle().merge(other)
        )
    }

    /**
     * Plus operator overload that applies a [merge].
     */
    @Stable
    operator fun plus(other: TextStyle): TextStyle = this.merge(other)

    /**
     * Plus operator overload that applies a [merge].
     */
    @Stable
    operator fun plus(other: ParagraphStyle): TextStyle = this.merge(other)

    /**
     * Plus operator overload that applies a [merge].
     */
    @Stable
    operator fun plus(other: SpanStyle): TextStyle = this.merge(other)

    fun copy(
        color: Color = this.spanStyle.color,
        fontSize: TextUnit = this.spanStyle.fontSize,
        fontWeight: FontWeight? = this.spanStyle.fontWeight,
        fontStyle: FontStyle? = this.spanStyle.fontStyle,
//        fontSynthesis: FontSynthesis? = this.spanStyle.fontSynthesis,
        fontFamily: FontFamily? = this.spanStyle.fontFamily,
//        fontFeatureSettings: String? = this.spanStyle.fontFeatureSettings,
        letterSpacing: TextUnit = this.spanStyle.letterSpacing,
//        baselineShift: BaselineShift? = this.spanStyle.baselineShift,
//        textGeometricTransform: TextGeometricTransform? = this.spanStyle.textGeometricTransform,
//        localeList: LocaleList? = this.spanStyle.localeList,
        background: Color = this.spanStyle.background,
        textDecoration: TextDecoration? = this.spanStyle.textDecoration,
        shadow: Shadow? = this.spanStyle.shadow,
//        drawStyle: DrawStyle? = this.spanStyle.drawStyle,
        textAlign: TextAlign = this.paragraphStyle.textAlign,
//        textDirection: TextDirection = this.paragraphStyle.textDirection,
        lineHeight: TextUnit = this.paragraphStyle.lineHeight,
        textIndent: TextIndent? = this.paragraphStyle.textIndent,
//        platformStyle: PlatformTextStyle? = this.platformStyle,
//        lineHeightStyle: LineHeightStyle? = this.paragraphStyle.lineHeightStyle,
//        lineBreak: LineBreak = this.paragraphStyle.lineBreak,
//        hyphens: Hyphens = this.paragraphStyle.hyphens,
//        textMotion: TextMotion? = this.paragraphStyle.textMotion,
    ): TextStyle {
        return TextStyle(
            spanStyle = SpanStyle(
                textForegroundStyle = if (color == this.spanStyle.color) {
                    spanStyle.textForegroundStyle
                } else {
                    TextForegroundStyle.from(color)
                },
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
//                fontSynthesis = fontSynthesis,
                fontFamily = fontFamily,
//                fontFeatureSettings = fontFeatureSettings,
                letterSpacing = letterSpacing,
//                baselineShift = baselineShift,
//                textGeometricTransform = textGeometricTransform,
//                localeList = localeList,
                background = background,
                textDecoration = textDecoration,
                shadow = shadow,
//                platformStyle = platformStyle?.spanStyle,
//                drawStyle = drawStyle
            ),
            paragraphStyle = ParagraphStyle(
                textAlign = textAlign,
//                textDirection = textDirection,
                lineHeight = lineHeight,
                textIndent = textIndent,
//                platformStyle = platformStyle?.paragraphStyle,
//                lineHeightStyle = lineHeightStyle,
//                lineBreak = lineBreak,
//                hyphens = hyphens,
//                textMotion = textMotion
            ),
//            platformStyle = platformStyle
        )
    }

    fun copy(
        brush: Brush?,
        alpha: Float = this.spanStyle.alpha,
        fontSize: TextUnit = this.spanStyle.fontSize,
        fontWeight: FontWeight? = this.spanStyle.fontWeight,
        fontStyle: FontStyle? = this.spanStyle.fontStyle,
//        fontSynthesis: FontSynthesis? = this.spanStyle.fontSynthesis,
        fontFamily: FontFamily? = this.spanStyle.fontFamily,
//        fontFeatureSettings: String? = this.spanStyle.fontFeatureSettings,
        letterSpacing: TextUnit = this.spanStyle.letterSpacing,
//        baselineShift: BaselineShift? = this.spanStyle.baselineShift,
//        textGeometricTransform: TextGeometricTransform? = this.spanStyle.textGeometricTransform,
//        localeList: LocaleList? = this.spanStyle.localeList,
        background: Color = this.spanStyle.background,
        textDecoration: TextDecoration? = this.spanStyle.textDecoration,
        shadow: Shadow? = this.spanStyle.shadow,
//        drawStyle: DrawStyle? = this.spanStyle.drawStyle,
        textAlign: TextAlign = this.paragraphStyle.textAlign,
//        textDirection: TextDirection = this.paragraphStyle.textDirection,
        lineHeight: TextUnit = this.paragraphStyle.lineHeight,
        textIndent: TextIndent? = this.paragraphStyle.textIndent,
//        platformStyle: PlatformTextStyle? = this.platformStyle,
//        lineHeightStyle: LineHeightStyle? = this.paragraphStyle.lineHeightStyle,
//        lineBreak: LineBreak = this.paragraphStyle.lineBreak,
//        hyphens: Hyphens = this.paragraphStyle.hyphens,
//        textMotion: TextMotion? = this.paragraphStyle.textMotion,
    ): TextStyle {
        return TextStyle(
            spanStyle = SpanStyle(
                brush = brush,
                alpha = alpha,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
//                fontSynthesis = fontSynthesis,
                fontFamily = fontFamily,
//                fontFeatureSettings = fontFeatureSettings,
                letterSpacing = letterSpacing,
//                baselineShift = baselineShift,
//                textGeometricTransform = textGeometricTransform,
//                localeList = localeList,
                background = background,
                textDecoration = textDecoration,
                shadow = shadow,
//                platformStyle = platformStyle?.spanStyle,
//                drawStyle = drawStyle
            ),
            paragraphStyle = ParagraphStyle(
                textAlign = textAlign,
//                textDirection = textDirection,
                lineHeight = lineHeight,
                textIndent = textIndent,
//                platformStyle = platformStyle?.paragraphStyle,
//                lineHeightStyle = lineHeightStyle,
//                lineBreak = lineBreak,
//                hyphens = hyphens,
//                textMotion = textMotion
            ),
//            platformStyle = platformStyle
        )
    }

    /**
     * The brush to use when drawing text. If not null, overrides [color].
     */
    val brush: Brush? get() = this.spanStyle.brush

    /**
     * The text color.
     */
    val color: Color get() = this.spanStyle.color

    /**
     * Opacity of text. This value is either provided along side Brush, or via alpha channel in
     * color.
     */
    val alpha: Float get() = this.spanStyle.alpha

    /**
     * The size of glyphs to use when painting the text. This
     * may be [TextUnit.Unspecified] for inheriting from another [TextStyle].
     */
    val fontSize: TextUnit get() = this.spanStyle.fontSize

    /**
     * The typeface thickness to use when painting the text (e.g., bold).
     */
    val fontWeight: FontWeight? get() = this.spanStyle.fontWeight

    /**
     * The typeface variant to use when drawing the letters (e.g., italic).
     */
    val fontStyle: FontStyle? get() = this.spanStyle.fontStyle

    /**
     * Whether to synthesize font weight and/or style when the requested weight or
     *  style cannot be found in the provided font family.
     */
//    val fontSynthesis: FontSynthesis? get() = this.spanStyle.fontSynthesis

    /**
     * The font family to be used when rendering the text.
     */
    val fontFamily: FontFamily? get() = this.spanStyle.fontFamily

    /**
     * The advanced typography settings provided by font. The format is the
     *  same as the CSS font-feature-settings attribute:
     *  https://www.w3.org/TR/css-fonts-3/#font-feature-settings-prop
     */
//    val fontFeatureSettings: String? get() = this.spanStyle.fontFeatureSettings

    /**
     * The amount of space to add between each letter.
     */
    val letterSpacing: TextUnit get() = this.spanStyle.letterSpacing

    /**
     * The amount by which the text is shifted up from the current baseline.
     */
//    val baselineShift: BaselineShift? get() = this.spanStyle.baselineShift

    /**
     * The geometric transformation applied the text.
     */
//    val textGeometricTransform: TextGeometricTransform? get() =
//        this.spanStyle.textGeometricTransform

//    /**
//     * The locale list used to select region-specific glyphs.
//     */
//    val localeList: LocaleList? get() = this.spanStyle.localeList

    /**
     * The background color for the text.
     */
    val background: Color get() = this.spanStyle.background

    /**
     * The decorations to paint on the text (e.g., an underline).
     */
    val textDecoration: TextDecoration? get() = this.spanStyle.textDecoration

    /**
     * The shadow effect applied on the text.
     */
    val shadow: Shadow? get() = this.spanStyle.shadow

    /**
     * Drawing style of text, whether fill in the text while drawing or stroke around the edges.
     */
//    val drawStyle: DrawStyle? get() = this.spanStyle.drawStyle

    /**
     * The alignment of the text within the lines of the paragraph.
     */
    val textAlign: TextAlign get() = this.paragraphStyle.textAlign

    @Deprecated("Kept for backwards compatibility.", level = DeprecationLevel.WARNING)
    @get:JvmName("getTextAlign-buA522U") // b/320819734
    @Suppress("unused", "RedundantNullableReturnType", "PropertyName")
    val deprecated_boxing_textAlign: TextAlign? get() = this.textAlign

    /**
     * The algorithm to be used to resolve the final text and paragraph
     * direction: Left To Right or Right To Left. If no value is provided the system will use the
     * [LayoutDirection] as the primary signal.
     */
//    val textDirection: TextDirection get() = this.paragraphStyle.textDirection

//    @Deprecated("Kept for backwards compatibility.", level = DeprecationLevel.WARNING)
//    @get:JvmName("getTextDirection-mmuk1to") // b/320819734
//    @Suppress("unused", "RedundantNullableReturnType", "PropertyName")
//    val deprecated_boxing_textDirection: TextDirection? get() = this.textDirection

    /**
     * Line height for the [Paragraph] in [TextUnit] unit, e.g. SP or EM.
     */
    val lineHeight: TextUnit get() = this.paragraphStyle.lineHeight

    /**
     * The indentation of the paragraph.
     */
    val textIndent: TextIndent? get() = this.paragraphStyle.textIndent

    /**
     * The configuration for line height such as vertical alignment of the line, whether to apply
     * additional space as a result of line height to top of first line top and bottom of last line.
     *
     * The configuration is applied only when a [lineHeight] is defined.
     *
     * When null, [LineHeightStyle.Default] is used.
     */
//    val lineHeightStyle: LineHeightStyle? get() = this.paragraphStyle.lineHeightStyle

    /**
     * The hyphens configuration of the paragraph.
     */
//    val hyphens: Hyphens get() = this.paragraphStyle.hyphens

//    @Deprecated("Kept for backwards compatibility.", level = DeprecationLevel.WARNING)
//    @get:JvmName("getHyphens-EaSxIns") // b/320819734
//    @Suppress("unused", "RedundantNullableReturnType", "PropertyName")
//    val deprecated_boxing_hyphens: Hyphens? get() = this.hyphens

    /**
     * The line breaking configuration of the paragraph.
     */
//    val lineBreak: LineBreak get() = this.paragraphStyle.lineBreak

//    @Deprecated("Kept for backwards compatibility.", level = DeprecationLevel.WARNING)
//    @get:JvmName("getLineBreak-LgCVezo") // b/320819734
//    @Suppress("unused", "RedundantNullableReturnType", "PropertyName")
//    val deprecated_boxing_lineBreak: LineBreak? get() = this.lineBreak

    /**
     * Text character placement configuration, whether to optimize for animated or static text.
     */
//    val textMotion: TextMotion? get() = this.paragraphStyle.textMotion

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextStyle) return false

        if (spanStyle != other.spanStyle) return false
        if (paragraphStyle != other.paragraphStyle) return false
//        if (platformStyle != other.platformStyle) return false

        return true
    }

    /**
     * Returns true if text layout affecting attributes between this TextStyle and other are the
     * same.
     *
     * The attributes that do not require a layout change are color, textDecoration and shadow.
     *
     * Majority of attributes change text layout, and examples are line height, font properties,
     * font size, locale etc.
     *
     * This function can be used to identify if a new text layout is required for a given TextStyle.
     *
     * @param other The TextStyle to compare to.
     */
    fun hasSameLayoutAffectingAttributes(other: TextStyle): Boolean {
        return (this === other) || (paragraphStyle == other.paragraphStyle &&
                spanStyle.hasSameLayoutAffectingAttributes(other.spanStyle))
    }

    fun hasSameDrawAffectingAttributes(other: TextStyle): Boolean {
        return (this === other) || (spanStyle.hasSameNonLayoutAttributes(other.spanStyle))
    }

    override fun hashCode(): Int {
        var result = spanStyle.hashCode()
        result = 31 * result + paragraphStyle.hashCode()
//        result = 31 * result + (platformStyle?.hashCode() ?: 0)
        return result
    }

    internal fun hashCodeLayoutAffectingAttributes(): Int {
        var result = spanStyle.hashCodeLayoutAffectingAttributes()
        result = 31 * result + paragraphStyle.hashCode()
//        result = 31 * result + (platformStyle?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return buildString {
            append("TextStyle(")
            append("color=$color, ")
            append("brush=$brush, ")
            append("alpha=$alpha, ")
            append("fontSize=$fontSize, ")
            append("fontWeight=$fontWeight, ")
            append("fontStyle=$fontStyle, ")
//            append("fontSynthesis=$fontSynthesis, ")
            append("fontFamily=$fontFamily, ")
//            append("fontFeatureSettings=$fontFeatureSettings, ")
            append("letterSpacing=$letterSpacing, ")
//            append("baselineShift=$baselineShift, ")
//            append("textGeometricTransform=$textGeometricTransform, ")
//            append("localeList=$localeList, ")
            append("background=$background, ")
            append("textDecoration=$textDecoration, ")
            append("shadow=$shadow, ")
//            append("drawStyle=$drawStyle, ")
            append("textAlign=$textAlign, ")
//            append("textDirection=$textDirection, ")
            append("lineHeight=$lineHeight, ")
            append("textIndent=$textIndent, ")
//            append("platformStyle=$platformStyle, ")
//            append("lineHeightStyle=$lineHeightStyle, ")
//            append("lineBreak=$lineBreak, ")
//            append("hyphens=$hyphens, ")
//            append("textMotion=$textMotion")
            append(")")
        }
    }

    companion object {
        /**
         * Constant for default text style.
         */
        @Stable
        val Default = TextStyle()
    }
}

/**
 * Interpolate between two text styles.
 *
 * This will not work well if the styles don't set the same fields.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 */
fun lerp(start: TextStyle, stop: TextStyle, fraction: Float): TextStyle {
    return TextStyle(
        spanStyle = lerp(start.toSpanStyle(), stop.toSpanStyle(), fraction),
        paragraphStyle = lerp(start.toParagraphStyle(), stop.toParagraphStyle(), fraction)
    )
}

///**
// * Fills missing values in TextStyle with default values and resolve [TextDirection].
// *
// * This function will fill all null or [TextUnit.Unspecified] field with actual values.
// * @param style a text style to be resolved
// * @param direction a layout direction to be used for resolving text layout direction algorithm
// * @return resolved text style.
// */
fun resolveDefaults(style: TextStyle, direction: LayoutDirection) = TextStyle(
    spanStyle = resolveSpanStyleDefaults(style.spanStyle),
    paragraphStyle = resolveParagraphStyleDefaults(style.paragraphStyle, direction),
////    platformStyle = style.platformStyle
)

/**
 * If [textDirection] is null returns a [TextDirection] based on [layoutDirection].
 */
internal fun resolveTextDirection(
    layoutDirection: LayoutDirection,
    textDirection: TextDirection
): TextDirection {
    return when (textDirection) {
        TextDirection.Content -> when (layoutDirection) {
            LayoutDirection.Ltr -> TextDirection.ContentOrLtr
            LayoutDirection.Rtl -> TextDirection.ContentOrRtl
        }
        TextDirection.Unspecified -> when (layoutDirection) {
            LayoutDirection.Ltr -> TextDirection.Ltr
            LayoutDirection.Rtl -> TextDirection.Rtl
        }
        else -> textDirection
    }
}

//private fun createPlatformTextStyleInternal(
//    platformSpanStyle: PlatformSpanStyle?,
//    platformParagraphStyle: PlatformParagraphStyle?
//): PlatformTextStyle? {
//    return if (platformSpanStyle == null && platformParagraphStyle == null) {
//        null
//    } else {
//        createPlatformTextStyle(platformSpanStyle, platformParagraphStyle)
//    }
//}
