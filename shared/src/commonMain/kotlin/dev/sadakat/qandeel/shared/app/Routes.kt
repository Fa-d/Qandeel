package dev.sadakat.qandeel.shared.app

import kotlinx.serialization.Serializable

/** The three tabs. */
@Serializable
data object TabsRoute

/** One surah in the reader, opened at [ayah] (0 = the top). */
@Serializable
data class ReaderRoute(val surah: Int, val ayah: Int = 0)

/** The full player, over everything. */
@Serializable
data object PlayerRoute

/** Your listening. */
@Serializable
data object ProgressRoute

/** About and credits. */
@Serializable
data object AboutRoute
