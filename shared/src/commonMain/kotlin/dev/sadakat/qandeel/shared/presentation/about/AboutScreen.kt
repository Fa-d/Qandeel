package dev.sadakat.qandeel.shared.presentation.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import dev.sadakat.qandeel.shared.designsystem.Celestial
import dev.sadakat.qandeel.shared.designsystem.effects.CelestialSky
import dev.sadakat.qandeel.shared.designsystem.kit.CelestialIcons
import dev.sadakat.qandeel.shared.designsystem.kit.Eyebrow
import dev.sadakat.qandeel.shared.designsystem.kit.GlassSurface
import dev.sadakat.qandeel.shared.designsystem.kit.GlyphButton
import dev.sadakat.qandeel.shared.presentation.components.ScreenHeader
import dev.sadakat.qandeel.shared.resources.Res
import dev.sadakat.qandeel.shared.resources.about_back
import dev.sadakat.qandeel.shared.resources.about_privacy
import dev.sadakat.qandeel.shared.resources.about_section_privacy
import dev.sadakat.qandeel.shared.resources.about_section_recitation
import dev.sadakat.qandeel.shared.resources.about_section_software
import dev.sadakat.qandeel.shared.resources.about_section_text
import dev.sadakat.qandeel.shared.resources.about_section_words
import dev.sadakat.qandeel.shared.resources.about_tagline
import dev.sadakat.qandeel.shared.resources.about_title
import dev.sadakat.qandeel.shared.resources.about_version
import dev.sadakat.qandeel.shared.resources.credit_alafasy
import dev.sadakat.qandeel.shared.resources.credit_alafasy_detail
import dev.sadakat.qandeel.shared.resources.credit_amiri
import dev.sadakat.qandeel.shared.resources.credit_amiri_detail
import dev.sadakat.qandeel.shared.resources.credit_baezeed
import dev.sadakat.qandeel.shared.resources.credit_baezeed_detail
import dev.sadakat.qandeel.shared.resources.credit_basit
import dev.sadakat.qandeel.shared.resources.credit_basit_detail
import dev.sadakat.qandeel.shared.resources.credit_fraunces
import dev.sadakat.qandeel.shared.resources.credit_fraunces_detail
import dev.sadakat.qandeel.shared.resources.credit_islamic_foundation
import dev.sadakat.qandeel.shared.resources.credit_islamic_foundation_detail
import dev.sadakat.qandeel.shared.resources.credit_libraries
import dev.sadakat.qandeel.shared.resources.credit_libraries_detail
import dev.sadakat.qandeel.shared.resources.credit_muhiuddin
import dev.sadakat.qandeel.shared.resources.credit_muhiuddin_detail
import dev.sadakat.qandeel.shared.resources.credit_quran_align
import dev.sadakat.qandeel.shared.resources.credit_quran_align_detail
import dev.sadakat.qandeel.shared.resources.credit_quran_com
import dev.sadakat.qandeel.shared.resources.credit_quran_com_detail
import dev.sadakat.qandeel.shared.resources.credit_sahih
import dev.sadakat.qandeel.shared.resources.credit_sahih_detail
import dev.sadakat.qandeel.shared.resources.credit_sudais
import dev.sadakat.qandeel.shared.resources.credit_sudais_detail
import dev.sadakat.qandeel.shared.resources.credit_toha
import dev.sadakat.qandeel.shared.resources.credit_toha_detail
import dev.sadakat.qandeel.shared.resources.credit_uthmani
import dev.sadakat.qandeel.shared.resources.credit_uthmani_detail
import dev.sadakat.qandeel.shared.resources.credit_walk
import dev.sadakat.qandeel.shared.resources.credit_walk_detail
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private class Credit(val title: StringResource, val detail: StringResource)

private val sections: List<Pair<StringResource, List<Credit>>> = listOf(
    Res.string.about_section_recitation to listOf(
        Credit(Res.string.credit_alafasy, Res.string.credit_alafasy_detail),
        Credit(Res.string.credit_walk, Res.string.credit_walk_detail),
        Credit(Res.string.credit_islamic_foundation, Res.string.credit_islamic_foundation_detail),
        Credit(Res.string.credit_toha, Res.string.credit_toha_detail),
        Credit(Res.string.credit_basit, Res.string.credit_basit_detail),
        Credit(Res.string.credit_baezeed, Res.string.credit_baezeed_detail),
        Credit(Res.string.credit_sudais, Res.string.credit_sudais_detail),
    ),
    Res.string.about_section_text to listOf(
        Credit(Res.string.credit_uthmani, Res.string.credit_uthmani_detail),
        Credit(Res.string.credit_sahih, Res.string.credit_sahih_detail),
        Credit(Res.string.credit_muhiuddin, Res.string.credit_muhiuddin_detail),
    ),
    Res.string.about_section_words to listOf(
        Credit(Res.string.credit_quran_align, Res.string.credit_quran_align_detail),
        Credit(Res.string.credit_quran_com, Res.string.credit_quran_com_detail),
    ),
    Res.string.about_section_software to listOf(
        Credit(Res.string.credit_amiri, Res.string.credit_amiri_detail),
        Credit(Res.string.credit_fraunces, Res.string.credit_fraunces_detail),
        Credit(Res.string.credit_libraries, Res.string.credit_libraries_detail),
    ),
)

/**
 * The app's version, then where every voice, text, timing and font it plays or shows comes from
 * (the CC BY word timings and the OFL fonts are credited here), and what it does with your data.
 */
@Composable
fun AboutScreen(versionName: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Celestial.colors
    Box(modifier.fillMaxSize().testTag("about")) {
        CelestialSky(Modifier.fillMaxSize(), glowCenter = Offset(0.5f, 0f))
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Celestial.spacing.gutter),
        ) {
            GlyphButton(CelestialIcons.Back, stringResource(Res.string.about_back), onBack)
            ScreenHeader(
                stringResource(Res.string.about_title),
                subtitle = stringResource(Res.string.about_version, versionName),
            )
            Spacer(Modifier.height(Celestial.spacing.sm))
            BasicText(
                stringResource(Res.string.about_tagline),
                style = Celestial.type.body.copy(color = colors.inkMuted),
            )
            sections.forEach { (title, credits) ->
                Spacer(Modifier.height(Celestial.spacing.xl))
                Eyebrow(stringResource(title))
                Spacer(Modifier.height(Celestial.spacing.sm))
                GlassSurface(Modifier.fillMaxWidth(), radius = Celestial.shapes.tile) {
                    Column(Modifier.padding(Celestial.spacing.lg)) {
                        credits.forEachIndexed { index, credit ->
                            if (index > 0) Spacer(Modifier.height(Celestial.spacing.md))
                            BasicText(
                                stringResource(credit.title),
                                style = Celestial.type.title.copy(color = colors.ink),
                            )
                            BasicText(
                                stringResource(credit.detail),
                                style = Celestial.type.caption.copy(color = colors.inkMuted),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(Celestial.spacing.xl))
            Eyebrow(stringResource(Res.string.about_section_privacy))
            Spacer(Modifier.height(Celestial.spacing.sm))
            BasicText(
                stringResource(Res.string.about_privacy),
                style = Celestial.type.body.copy(color = colors.inkMuted),
            )
            Spacer(Modifier.height(Celestial.spacing.xxl))
        }
    }
}
