package org.neteinstein.family.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.neteinstein.family.domain.usecase.MarkAllQuestionsAsUnusedUseCase

class SettingsViewModel(
    private val markAllQuestionsAsUnusedUseCase: MarkAllQuestionsAsUnusedUseCase
) : ViewModel() {

    fun markAllCardsAsUnused() {
        viewModelScope.launch {
            markAllQuestionsAsUnusedUseCase()
        }
    }
}
