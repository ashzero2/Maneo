package com.maneo.app.feature.blocker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.data.prefs.SeenVerseRepository
import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.blocker.domain.GetPrayerForDate
import com.maneo.app.feature.blocker.domain.Prayer
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InterceptViewModel @Inject constructor(
    private val getVerseForSlot: GetVerseForSlot,
    private val getPrayerForDate: GetPrayerForDate,
    private val seenVerseRepository: SeenVerseRepository,
) : ViewModel() {

    private val _verse = MutableStateFlow<Verse?>(null)
    val verse: StateFlow<Verse?> = _verse.asStateFlow()

    private val _prayer = MutableStateFlow<Prayer?>(null)
    val prayer: StateFlow<Prayer?> = _prayer.asStateFlow()

    init {
        viewModelScope.launch {
            val slot = "intercept"
            val date = LocalDate.now()

            val seenVerseIds = seenVerseRepository.getSeenVerseIds(slot, date)
            val v = getVerseForSlot(slot, seenIds = seenVerseIds)
            seenVerseRepository.markVerseSeen(slot, v.id, date)
            _verse.value = v

            val seenPrayerIds = seenVerseRepository.getSeenPrayerIds(date)
            val p = getPrayerForDate(seenIds = seenPrayerIds)
            seenVerseRepository.markPrayerSeen(p.id, date)
            _prayer.value = p
        }
    }
}
