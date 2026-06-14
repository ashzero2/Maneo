package com.maneo.app.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.blocker.repository.BlockedAppsRepository
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getVerseForSlot: GetVerseForSlot,
    blockedAppsRepository: BlockedAppsRepository,
) : ViewModel() {

    val verse: Verse = getVerseForSlot("morning")

    val blockedCount: StateFlow<Int> = blockedAppsRepository.blockedApps
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
