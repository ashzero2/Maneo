package com.maneo.app.feature.blocker.ui

import androidx.lifecycle.ViewModel
import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.blocker.domain.GetPrayerForDate
import com.maneo.app.feature.blocker.domain.Prayer
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InterceptViewModel @Inject constructor(
    getVerseForSlot: GetVerseForSlot,
    getPrayerForDate: GetPrayerForDate,
) : ViewModel() {

    val verse: Verse = getVerseForSlot("intercept")
    val prayer: Prayer = getPrayerForDate()
}
