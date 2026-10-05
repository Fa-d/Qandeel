package dev.sadakat.qandeel.shared.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.FloatingPlayerBar
import dev.sadakat.qandeel.shared.designsystem.kit.FloatingTabBar
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.OctagramBadge
import dev.sadakat.qandeel.shared.designsystem.kit.PillTabs
import dev.sadakat.qandeel.shared.designsystem.kit.PlayButton
import dev.sadakat.qandeel.shared.designsystem.kit.PlayerBarLabels
import dev.sadakat.qandeel.shared.designsystem.kit.ProgressLine
import dev.sadakat.qandeel.shared.designsystem.kit.SearchField
import dev.sadakat.qandeel.shared.designsystem.kit.TabItem
import dev.sadakat.qandeel.shared.designsystem.lamp.LampMotion
import dev.sadakat.qandeel.shared.designsystem.lamp.QandeelLamp

// Concept screens for reviewing the look (Phase 2): the kit composed the way Phase 4's screens will
// be, with sample text. The real screens replace these.

private val tabs = listOf(
    TabItem(CelestialIcons.Home, "Home"),
    TabItem(CelestialIcons.Quran, "Quran"),
    TabItem(CelestialIcons.You, "You"),
)

private val playerLabels = PlayerBarLabels(play = "Play", pause = "Pause", next = "Next ayah", expand = "Open player")

@Composable
internal fun HomeConcept() {
    val clock = Celestial.clock
    Box(Modifier.fillMaxSize()) {
        CelestialSky(Modifier.fillMaxSize(), glowCenter = Offset(0.5f, 0.22f))
        QandeelLamp(
            motion = { LampMotion(time = clock.seconds(), energy = 0.4f) },
            colors = Celestial.colors.lamp,
            modifier = Modifier
                .padding(top = 60.dp)
                .fillMaxWidth()
                .height(290.dp),
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = Celestial.spacing.gutter),
        ) {
            Spacer(Modifier.height(36.dp))
            Wordmark()
            Spacer(Modifier.height(226.dp))
            ContinueCard()
            Spacer(Modifier.height(Celestial.spacing.xl))
            Eyebrow("Recently heard")
            Spacer(Modifier.height(Celestial.spacing.md))
            // Wider than the screen on purpose: the row scrolls, and the cut-off tile says so.
            Row(
                Modifier.wrapContentWidth(align = Alignment.Start, unbounded = true),
                horizontalArrangement = Arrangement.spacedBy(Celestial.spacing.md),
            ) {
                RecentTile(36, "Ya-Sin", "3 rounds")
                RecentTile(67, "Al-Mulk", "1 round · 40%")
                RecentTile(55, "Ar-Rahman", "62%")
            }
        }
        BottomChrome(selectedTab = 0, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Wordmark() {
    val colors = Celestial.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicText("Qandeel", style = Celestial.type.headline.copy(color = colors.ink, fontSize = 24.sp))
        Spacer(Modifier.weight(1f))
        BasicText("قنديل", style = Celestial.quran.label.copy(color = colors.accent))
    }
}

@Composable
private fun ContinueCard() {
    val colors = Celestial.colors
    GlassSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Celestial.spacing.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Continue listening")
                Spacer(Modifier.weight(1f))
                BasicText("Ayah 10 of 110", style = Celestial.type.caption.copy(color = colors.inkMuted))
            }
            Spacer(Modifier.height(Celestial.spacing.md))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    BasicText("Al-Kahf", style = Celestial.type.display.copy(color = colors.ink))
                    BasicText("The Cave · Arabic + Bangla", style = Celestial.type.body.copy(color = colors.inkMuted))
                }
                BasicText("الكهف", style = Celestial.quran.title.copy(color = colors.arabic))
            }
            Spacer(Modifier.height(Celestial.spacing.lg))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressLine(
                    0.09f,
                    Modifier
                        .weight(1f)
                        .height(4.dp),
                )
                Spacer(Modifier.width(Celestial.spacing.lg))
                PlayButton(playing = false, label = "Play", onClick = {})
            }
        }
    }
}

@Composable
private fun RecentTile(number: Int, name: String, meta: String, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    GlassSurface(modifier.width(150.dp), radius = Celestial.shapes.tile) {
        Column(Modifier.padding(Celestial.spacing.lg)) {
            OctagramBadge(number, size = 38.dp)
            Spacer(Modifier.height(Celestial.spacing.md))
            BasicText(name, style = Celestial.type.title.copy(color = colors.ink), maxLines = 1)
            BasicText(meta, style = Celestial.type.caption.copy(color = colors.inkMuted), maxLines = 1)
        }
    }
}

@Composable
private fun BottomChrome(selectedTab: Int, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Celestial.spacing.lg)
            .padding(bottom = Celestial.spacing.xl),
    ) {
        FloatingPlayerBar(
            surahNumber = 18,
            title = "Al-Kahf",
            subtitle = "Ayah 10 · Arabic + Bangla",
            progress = 0.09f,
            playing = true,
            labels = playerLabels,
            onTogglePlay = {},
            onNext = {},
            onExpand = {},
        )
        Spacer(Modifier.height(Celestial.spacing.sm))
        FloatingTabBar(tabs, selected = selectedTab, onSelect = {})
    }
}

private enum class Browse(val label: String) { SURAH("Surah"), JUZ("Juz"), OFFLINE("Offline") }

private class SurahSample(val number: Int, val name: String, val meaning: String, val arabic: String, val meta: String)

private val surahSamples = listOf(
    SurahSample(1, "Al-Fatiha", "The Opening", "الفاتحة", "Meccan · 7 ayahs"),
    SurahSample(2, "Al-Baqarah", "The Cow", "البقرة", "Medinan · 286 ayahs"),
    SurahSample(3, "Ali 'Imran", "Family of Imran", "آل عمران", "Medinan · 200 ayahs"),
    SurahSample(4, "An-Nisa", "The Women", "النساء", "Medinan · 176 ayahs"),
    SurahSample(5, "Al-Ma'idah", "The Table Spread", "المائدة", "Medinan · 120 ayahs"),
    SurahSample(6, "Al-An'am", "The Cattle", "الأنعام", "Meccan · 165 ayahs"),
    SurahSample(7, "Al-A'raf", "The Heights", "الأعراف", "Meccan · 206 ayahs"),
)

@Composable
internal fun QuranConcept() {
    val colors = Celestial.colors
    Box(Modifier.fillMaxSize()) {
        CelestialSky(Modifier.fillMaxSize(), glowCenter = Offset(0.85f, 0.02f))
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = Celestial.spacing.gutter),
        ) {
            Spacer(Modifier.height(48.dp))
            BasicText("The Quran", style = Celestial.type.headline.copy(color = colors.ink))
            BasicText("114 surahs · 30 juz", style = Celestial.type.caption.copy(color = colors.inkMuted))
            Spacer(Modifier.height(Celestial.spacing.lg))
            SearchField(query = "", onQueryChange = {
            }, placeholder = "Surah, number or 2:255", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Celestial.spacing.md))
            PillTabs(Browse.entries, Browse.SURAH, onSelect = {
            }, label = { it.label }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Celestial.spacing.md))
            surahSamples.forEach { SurahRowConcept(it) }
        }
        BottomChrome(selectedTab = 1, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SurahRowConcept(surah: SurahSample) {
    val colors = Celestial.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Celestial.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OctagramBadge(surah.number)
        Spacer(Modifier.width(Celestial.spacing.lg))
        Column(Modifier.weight(1f)) {
            BasicText(surah.name, style = Celestial.type.title.copy(color = colors.ink))
            BasicText("${surah.meaning} · ${surah.meta}", style = Celestial.type.caption.copy(color = colors.inkMuted))
        }
        BasicText(surah.arabic, style = Celestial.quran.label.copy(color = colors.arabic))
    }
}
