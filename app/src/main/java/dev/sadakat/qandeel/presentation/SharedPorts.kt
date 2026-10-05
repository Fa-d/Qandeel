package dev.sadakat.qandeel.presentation

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sadakat.qandeel.core.domain.player.QuranPlayer
import dev.sadakat.qandeel.core.domain.repository.ListeningHistory
import dev.sadakat.qandeel.core.domain.repository.QuranSettings
import dev.sadakat.qandeel.core.domain.repository.QuranText
import dev.sadakat.qandeel.core.domain.repository.SurahDownloads

/**
 * The domain ports the multiplatform ViewModels (`:shared`) are built from. They can't use Hilt
 * themselves, so the phone app hands them what Hilt provides.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharedPorts {
    fun quranText(): QuranText

    fun settings(): QuranSettings

    fun downloads(): SurahDownloads

    fun player(): QuranPlayer

    fun history(): ListeningHistory
}
