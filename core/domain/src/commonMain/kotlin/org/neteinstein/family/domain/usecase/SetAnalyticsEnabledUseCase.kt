package org.neteinstein.family.domain.usecase

import org.neteinstein.family.domain.repository.AnalyticsPreferenceRepository

/**
 * Records the user's "Share usage data" choice. Persisting it is all this does - actually
 * stopping or restarting collection is `ConsentAwareAnalyticsTracker`'s job, which observes the
 * same repository and drives the SDK-level switch.
 */
class SetAnalyticsEnabledUseCase(
    private val repository: AnalyticsPreferenceRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAnalyticsEnabled(enabled)
}
