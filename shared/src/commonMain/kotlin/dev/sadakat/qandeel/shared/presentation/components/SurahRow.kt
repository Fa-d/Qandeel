package dev.sadakat.qandeel.shared.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sadakat.qandeel.core.domain.model.Revelation
import dev.sadakat.qandeel.core.domain.repository.SurahDownloadState
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.kit.OctagramBadge
import dev.sadakat.qandeel.shared.presentation.home.SurahRowUi
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.quran_cd_download_failed
import dev.sadakat.qandeel.shared.resources.quran_cd_downloaded
import dev.sadakat.qandeel.shared.resources.quran_cd_downloading
import dev.sadakat.qandeel.shared.resources.quran_cd_playing
import dev.sadakat.qandeel.shared.resources.quran_meccan
import dev.sadakat.qandeel.shared.resources.quran_medinan
import dev.sadakat.qandeel.shared.resources.quran_surah_subtitle
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * A surah in a list: its number in the star, its name and what it is, its Arabic name, and a
 * small mark for whether it is on the phone. The playing surah's name is lit gold.
 */
@Composable
fun SurahRow(row: SurahRowUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    val surah = row.surah
    val revelation = stringResource(if (surah.revelation == Revelation.MECCAN) Res.string.quran_meccan else Res.string.quran_medinan)
    val playing = stringResource(Res.string.quran_cd_playing)
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Celestial.spacing.gutter, vertical = Celestial.spacing.md)
            .semantics(mergeDescendants = true) { if (row.isPlaying) contentDescription = playing },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OctagramBadge(surah.number)
        Spacer(Modifier.width(Celestial.spacing.lg))
        Column(Modifier.weight(1f)) {
            BasicText(
                surah.nameEnglish,
                style = Celestial.type.title.copy(color = if (row.isPlaying) colors.accent else colors.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                stringResource(Res.string.quran_surah_subtitle, surah.meaningEnglish, revelation, surah.ayahCount),
                style = Celestial.type.caption.copy(color = colors.inkMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(Celestial.spacing.sm))
        BasicText(surah.nameArabicShort, style = Celestial.quran.label.copy(color = colors.arabic))
        DownloadMark(row.download)
    }
}

/** A tiny ring: full when on the phone, filling while it downloads, broken when it failed. */
@Composable
private fun DownloadMark(state: SurahDownloadState, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    val description = when (state) {
        SurahDownloadState.NotDownloaded -> return
        SurahDownloadState.Downloaded -> stringResource(Res.string.quran_cd_downloaded)
        is SurahDownloadState.Downloading -> stringResource(Res.string.quran_cd_downloading, (state.progress * 100).roundToInt())
        is SurahDownloadState.Failed -> stringResource(Res.string.quran_cd_download_failed)
    }
    val (fraction, color) = when (state) {
        is SurahDownloadState.Downloading -> state.progress to colors.accent
        is SurahDownloadState.Failed -> 1f to colors.inkFaint
        else -> 1f to colors.accent
    }
    Box(
        modifier
            .padding(start = Celestial.spacing.md)
            .size(10.dp)
            .semantics { contentDescription = description }
            .drawBehind {
                val stroke = 2.dp.toPx()
                val arc = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(colors.inkFaint.copy(alpha = 0.3f), 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
                drawArc(color, -90f, 360f * fraction, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
            },
    )
}
