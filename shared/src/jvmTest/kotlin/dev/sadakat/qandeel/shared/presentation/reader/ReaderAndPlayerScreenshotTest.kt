// The sample ayahs are quoted whole, as bundled, one string per line.
@file:Suppress("MaxLineLength")

package dev.sadakat.qandeel.shared.presentation.reader

import dev.sadakat.qandeel.core.domain.model.Ayah
import dev.sadakat.qandeel.core.domain.model.RecitationMode
import dev.sadakat.qandeel.core.domain.model.Revelation
import dev.sadakat.qandeel.core.domain.model.Surah
import dev.sadakat.qandeel.core.domain.model.Track
import dev.sadakat.qandeel.core.domain.player.NowPlaying
import dev.sadakat.qandeel.core.domain.player.PlaybackProgress
import dev.sadakat.qandeel.core.domain.player.RepeatSetting
import dev.sadakat.qandeel.core.domain.player.WordPointer
import dev.sadakat.qandeel.core.domain.repository.SurahDownloadState
import dev.sadakat.qandeel.shared.designsystem.phoneSnapshot
import dev.sadakat.qandeel.shared.presentation.about.AboutScreen
import dev.sadakat.qandeel.shared.presentation.player.PlayerActions
import dev.sadakat.qandeel.shared.presentation.player.PlayerScreen
import dev.sadakat.qandeel.shared.presentation.player.PlayerUiState
import org.junit.Test

/** The reader reading and as lyrics, word by word, the full player, and About. */
class ReaderAndPlayerScreenshotTest {

    // Al-Kahf 18:1-6 as bundled (Uthmani, Saheeh International, Muhiuddin Khan).
    private val ayahs = listOf(
        Ayah(
            18,
            1,
            2141,
            "ٱلْحَمْدُ لِلَّهِ ٱلَّذِىٓ أَنزَلَ عَلَىٰ عَبْدِهِ ٱلْكِتَٰبَ وَلَمْ يَجْعَل لَّهُۥ عِوَجَا ۜ",
            "[All] praise is [due] to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.",
            "সব প্রশংসা আল্লাহর যিনি নিজের বান্দার প্রতি এ গ্রন্থ নাযিল করেছেন এবং তাতে কোন বক্রতা রাখেননি।",
        ),
        Ayah(
            18,
            2,
            2142,
            "قَيِّمًۭا لِّيُنذِرَ بَأْسًۭا شَدِيدًۭا مِّن لَّدُنْهُ وَيُبَشِّرَ ٱلْمُؤْمِنِينَ ٱلَّذِينَ يَعْمَلُونَ ٱلصَّٰلِحَٰتِ أَنَّ لَهُمْ أَجْرًا حَسَنًۭا",
            "[He has made it] straight, to warn of severe punishment from Him and to give good tidings to the believers who do righteous deeds that they will have a good reward",
            "একে সুপ্রতিষ্ঠিত করেছেন যা আল্লাহর পক্ষ থেকে একটি ভীষণ বিপদের ভয় প্রদর্শন করে এবং মুমিনদেরকে যারা সৎকর্ম সম্পাদন করে-তাদেরকে সুসংবাদ দান করে যে, তাদের জন্যে উত্তম প্রতিদান রয়েছে।",
        ),
        Ayah(
            18,
            3,
            2143,
            "مَّٰكِثِينَ فِيهِ أَبَدًۭا",
            "In which they will remain forever",
            "তারা তাতে চিরকাল অবস্থান করবে।",
        ),
        Ayah(
            18,
            4,
            2144,
            "وَيُنذِرَ ٱلَّذِينَ قَالُوا۟ ٱتَّخَذَ ٱللَّهُ وَلَدًۭا",
            "And to warn those who say, \"Allah has taken a son.\"",
            "এবং তাদেরকে ভয় প্রদর্শন করার জন্যে যারা বলে যে, আল্লাহর সন্তান রয়েছে।",
        ),
        Ayah(
            18,
            5,
            2145,
            "مَّا لَهُم بِهِۦ مِنْ عِلْمٍۢ وَلَا لِءَابَآئِهِمْ ۚ كَبُرَتْ كَلِمَةًۭ تَخْرُجُ مِنْ أَفْوَٰهِهِمْ ۚ إِن يَقُولُونَ إِلَّا كَذِبًۭا",
            "They have no knowledge of it, nor had their fathers. Grave is the word that comes out of their mouths; they speak not except a lie.",
            "এ সম্পর্কে তাদের কোন জ্ঞান নেই এবং তাদের পিতৃপুরুষদেরও নেই। কত কঠিন তাদের মুখের কথা। তারা যা বলে তা তো সবই মিথ্যা।",
        ),
        Ayah(
            18,
            6,
            2146,
            "فَلَعَلَّكَ بَٰخِعٌۭ نَّفْسَكَ عَلَىٰٓ ءَاثَٰرِهِمْ إِن لَّمْ يُؤْمِنُوا۟ بِهَٰذَا ٱلْحَدِيثِ أَسَفًا",
            "Then perhaps you would kill yourself through grief over them, [O Muhammad], if they do not believe in this message, [and] out of sorrow.",
            "যদি তারা এই বিষয়বস্তুর প্রতি বিশ্বাস স্থাপন না করে, তবে তাদের পশ্চাতে সম্ভবতঃ আপনি পরিতাপ করতে করতে নিজের প্রাণ নিপাত করবেন।",
        ),
    )

    // 18:2's word meanings as bundled (quran.com, English).
    private val meanings2 = listOf(
        "Straight",
        "to warn",
        "(of) a punishment",
        "severe",
        "from",
        "near Him",
        "and give glad tidings",
        "(to) the believers",
        "those who",
        "do",
        "righteous deeds",
        "that",
        "for them",
        "(is) a good reward",
        "(is) a good reward",
    )

    private val surah = Surah(18, "سُورَةُ ٱلْكَهْفِ", "Al-Kahf", "The Cave", 110, Revelation.MECCAN)

    private val reading = SurahReaderUiState(
        surah = surah,
        ayahs = ayahs,
        mode = RecitationMode.ARABIC_ENGLISH,
        downloadState = SurahDownloadState.Downloaded,
        heard = listOf(3, 3, 2, 1, 0, 0),
    )

    @Test
    fun readingNight() = phoneSnapshot("reader_reading_night", night = true) {
        ReaderScreen(reading, WordPointer.Off, ReaderActions())
    }

    @Test
    fun readingDawn() = phoneSnapshot("reader_reading_dawn", night = false) {
        ReaderScreen(
            reading.copy(downloadState = SurahDownloadState.Downloading(3, 10)),
            WordPointer.Off,
            ReaderActions(),
        )
    }

    @Test
    fun lyricsWhilePlaying() = phoneSnapshot("reader_lyrics_night", night = true, reduceMotion = true) {
        ReaderScreen(reading.copy(playingAyah = 2), WordPointer.Reciting(4), ReaderActions(), bottomPadding = BAR)
    }

    @Test
    fun wordByWord() = phoneSnapshot("reader_word_by_word_night", night = true, reduceMotion = true) {
        ReaderScreen(
            reading.copy(playingAyah = 2, wordMeanings = mapOf(2 to meanings2)),
            WordPointer.Reciting(2),
            ReaderActions(),
        )
    }

    private val playing = PlayerUiState(
        nowPlaying = NowPlaying(
            18,
            2,
            Track.ARABIC,
            RecitationMode.ARABIC_ENGLISH,
            isPlaying = true,
            isBuffering = false,
            repeat = RepeatSetting.Ayah(3),
        ),
        surahName = "Al-Kahf",
        ayahArabic = ayahs[1].arabic,
        ayahTranslation = ayahs[1].english,
        ayahMeanings = meanings2,
    )

    @Test
    fun playerNight() = phoneSnapshot("player_night", night = true, reduceMotion = true) {
        PlayerScreen(playing, WordPointer.Reciting(4), { PlaybackProgress(0, 83_000, 1_420_000) }, PlayerActions())
    }

    @Test
    fun playerDawn() = phoneSnapshot("player_dawn", night = false, reduceMotion = true) {
        PlayerScreen(
            playing.copy(ayahMeanings = emptyList()),
            WordPointer.Reciting(1),
            { PlaybackProgress(0, 83_000, 1_420_000) },
            PlayerActions(),
        )
    }

    @Test
    fun about() = phoneSnapshot("about_night", night = true) {
        AboutScreen(versionName = "2.0", onBack = {})
    }

    private companion object {
        val BAR = dev.sadakat.qandeel.shared.designsystem.CelestialShapes().barHeight
    }
}
