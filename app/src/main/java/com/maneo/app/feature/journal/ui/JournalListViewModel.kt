package com.maneo.app.feature.journal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.domain.model.JournalEntry
import com.maneo.app.feature.journal.domain.GetEntries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class JournalListViewModel @Inject constructor(getEntries: GetEntries) : ViewModel() {

    val entries: StateFlow<List<JournalEntry>> = getEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
