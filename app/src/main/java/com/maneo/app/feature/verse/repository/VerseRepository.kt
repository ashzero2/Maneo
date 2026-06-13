package com.maneo.app.feature.verse.repository

import com.maneo.app.core.domain.model.Verse

class VerseRepository(private val versesLoader: () -> List<Verse>) {

    private val verses: List<Verse> by lazy { versesLoader() }

    fun getVersesForSlot(slot: String): List<Verse> =
        verses.filter { slot in it.slots }
}
