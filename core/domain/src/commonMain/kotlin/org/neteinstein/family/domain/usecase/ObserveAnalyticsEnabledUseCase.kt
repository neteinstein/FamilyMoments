package org.neteinstein.family.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.family.domain.repository.AnalyticsPreferenceRepository

/** Emits the "Share usage data" choice and every later change, so the Settings switch stays in sync. */
class ObserveAnalyticsEnabledUseCase(
    private val repository: AnalyticsPreferenceRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.analyticsEnabled
}
