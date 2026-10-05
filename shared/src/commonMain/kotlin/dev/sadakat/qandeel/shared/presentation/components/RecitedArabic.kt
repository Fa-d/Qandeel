package dev.sadakat.qandeel.shared.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.core.domain.model.ArabicWords
import dev.sadakat.qandeel.core.domain.player.WordPointer
import dev.sadakat.qandeel.shared.designsystem.Celestial

/** The word pointer's inks: recited words, the word on the pill, words to come, and the pill. */
private class PointerInk(val recited: Color, val current: Color, val upcoming: Color, val pill: Color)

@Composable
private fun pointerInk(): PointerInk {
    val colors = Celestial.colors
    return PointerInk(
        recited = colors.arabic,
        current = colors.onAccent,
        upcoming = colors.arabic.copy(alpha = UPCOMING_ALPHA),
        pill = colors.accent,
    )
}

private const val UPCOMING_ALPHA = 0.42f

/**
 * An ayah's Arabic with the word pointer: words already recited in full ink, the word being recited
 * on a gold pill, words still to come quieter. While the translation is read the whole Arabic steps
 * back. The pill is drawn behind the text and only colours change, so the text never reflows as the
 * pointer moves.
 *
 * With [keepCurrentLineInView], the line holding the current word is scrolled into view by the
 * nearest scrolling parent (long ayahs outgrow the screen).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecitedArabicText(
    text: String,
    pointer: WordPointer,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Right,
    keepCurrentLineInView: Boolean = false,
) {
    val ink = pointerInk()
    val words = remember(text) { ArabicWords.ranges(text) }
    val currentWord = (pointer as? WordPointer.Reciting)?.let { words.getOrNull(it.word) }
    val annotated = remember(text, words, pointer, ink.current, ink.upcoming) { pointedText(text, words, pointer, ink) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val requester = remember { BringIntoViewRequester() }

    if (keepCurrentLineInView) {
        LaunchedEffect(pointer, layout, words) {
            val word = (pointer as? WordPointer.Reciting)?.word ?: return@LaunchedEffect
            val textLayout = layout ?: return@LaunchedEffect
            val range = words.getOrNull(word) ?: return@LaunchedEffect
            val line = textLayout.getLineForOffset(range.first)
            requester.bringIntoView(
                Rect(0f, textLayout.getLineTop(line), textLayout.size.width.toFloat(), textLayout.getLineBottom(line)),
            )
        }
    }
    BasicText(
        text = annotated,
        style = style.copy(color = ink.recited, textAlign = textAlign),
        onTextLayout = { layout = it },
        modifier = modifier
            .bringIntoViewRequester(requester)
            .drawBehind {
                val textLayout = layout ?: return@drawBehind
                // A layout of the previous text can outlive it by a frame.
                val range = currentWord?.takeIf { it.last < textLayout.layoutInput.text.length } ?: return@drawBehind
                val radius = CornerRadius(PILL_RADIUS_EM * style.fontSize.toPx())
                for (rect in wordPills(textLayout, range, style.fontSize.toPx())) {
                    drawRoundRect(color = ink.pill, topLeft = rect.topLeft, size = rect.size, cornerRadius = radius)
                }
            },
    )
}

/**
 * Where the pill behind the word at [range] goes: one rect per line the word is on (a pause mark
 * after a space can wrap to the next line), a little wider than the glyphs and as tall as the
 * stacked marks reach.
 */
private fun wordPills(layout: TextLayoutResult, range: IntRange, fontSizePx: Float): List<Rect> {
    val end = range.last + 1
    val padding = fontSizePx * PILL_PADDING_EM
    return (layout.getLineForOffset(range.first)..layout.getLineForOffset(range.last)).mapNotNull { line ->
        val start = maxOf(range.first, layout.getLineStart(line))
        val lineEnd = minOf(end, layout.getLineEnd(line, visibleEnd = true))
        if (start >= lineEnd) return@mapNotNull null
        val glyphs = layout.getPathForRange(start, lineEnd).getBounds()
        val baseline = layout.getLineBaseline(line)
        Rect(
            left = glyphs.left - padding,
            top = baseline - fontSizePx * PILL_ASCENT_EM,
            right = glyphs.right + padding,
            bottom = baseline + fontSizePx * PILL_DESCENT_EM,
        )
    }
}

/** How far the pill reaches past the word's glyphs on each side, in ems: less than half a space. */
private const val PILL_PADDING_EM = 0.18f

/**
 * How far the pill reaches above and below the baseline, in ems: Amiri Quran's letters sit low in
 * the tall line, with marks stacked above them and a kasra below.
 */
private const val PILL_ASCENT_EM = 1.15f
private const val PILL_DESCENT_EM = 0.6f
private const val PILL_RADIUS_EM = 0.45f

private fun pointedText(text: String, words: List<IntRange>, pointer: WordPointer, ink: PointerInk): AnnotatedString =
    when (pointer) {
        WordPointer.Off -> AnnotatedString(text)

        WordPointer.Translating -> buildAnnotatedString { withStyle(SpanStyle(color = ink.upcoming)) { append(text) } }

        is WordPointer.Reciting -> buildAnnotatedString {
            append(text)
            words.forEachIndexed { index, range ->
                val color = when {
                    index < pointer.word -> return@forEachIndexed

                    // recited: the text's own ink
                    index == pointer.word -> ink.current

                    else -> ink.upcoming
                }
                addStyle(SpanStyle(color = color), range.first, range.last + 1)
            }
        }
    }

/**
 * An ayah's Arabic word by word, each word over its meaning, flowing right to left, lit by the
 * word pointer as [RecitedArabicText] is. [meanings] holds one meaning per word of [text] as
 * [ArabicWords] splits it. With [onWordClick] a tap on a word reports its index (to play from it).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordByWordText(
    text: String,
    meanings: List<String>,
    pointer: WordPointer,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onWordClick: ((Int) -> Unit)? = null,
    onLongPress: () -> Unit = {},
) {
    val ink = pointerInk()
    val meaningInk = Celestial.colors.accent
    val words = remember(text) { ArabicWords.ranges(text).map { text.substring(it) } }
    val current = (pointer as? WordPointer.Reciting)?.word
    // Right to left like the Arabic itself: the first word starts the row at the right edge.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(Celestial.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Celestial.spacing.sm),
        ) {
            words.forEachIndexed { index, word ->
                val isCurrent = index == current
                val dimmed = pointer == WordPointer.Translating || (current != null && index > current)
                Column(
                    Modifier
                        .background(if (isCurrent) ink.pill else ink.pill.copy(alpha = 0f), RoundedCornerShape(12.dp))
                        .padding(horizontal = Celestial.spacing.xs, vertical = 2.dp)
                        .then(
                            if (onWordClick == null) {
                                Modifier
                            } else {
                                Modifier.pointerInput(index, onWordClick) {
                                    detectTapGestures(onTap = { onWordClick(index) }, onLongPress = { onLongPress() })
                                }
                            },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val wordInk = when {
                        isCurrent -> ink.current
                        dimmed -> ink.upcoming
                        else -> ink.recited
                    }
                    BasicText(word, style = style.copy(color = wordInk, textAlign = TextAlign.Center))
                    meanings.getOrNull(index)?.let { meaning ->
                        BasicText(
                            meaning,
                            style = Celestial.type.caption.copy(
                                color = if (isCurrent) {
                                    ink.current
                                } else {
                                    meaningInk.copy(
                                        alpha = if (dimmed) UPCOMING_ALPHA else 1f,
                                    )
                                },
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }
        }
    }
}
