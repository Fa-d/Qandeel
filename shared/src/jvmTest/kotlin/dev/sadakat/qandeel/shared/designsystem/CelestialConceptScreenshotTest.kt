package dev.sadakat.qandeel.shared.designsystem

import org.junit.Test

/** The look as a whole, for review: Home at night and at dawn, and the Quran tab. */
class CelestialConceptScreenshotTest {

    @Test
    fun homeNight() = phoneSnapshot("concept_home_night", night = true) { HomeConcept() }

    @Test
    fun homeDawn() = phoneSnapshot("concept_home_dawn", night = false) { HomeConcept() }

    @Test
    fun quranNight() = phoneSnapshot("concept_quran_night", night = true) { QuranConcept() }
}
