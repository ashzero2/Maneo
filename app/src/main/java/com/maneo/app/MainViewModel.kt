package com.maneo.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.feature.onboarding.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _onboardingDone = MutableStateFlow<Boolean?>(null)
    val onboardingDone: StateFlow<Boolean?> = _onboardingDone

    init {
        viewModelScope.launch {
            onboardingRepository.isOnboardingDone.collect { _onboardingDone.value = it }
        }
    }
}
