package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.AnalyticsPreferenceRepository

/** Reads the current "Share usage data" choice. Defaults to `true` - analytics is opt-out. */
class GetAnalyticsEnabledUseCase(
    private val repository: AnalyticsPreferenceRepository,
) {
    operator fun invoke(): Boolean = repository.isAnalyticsEnabled()
}
